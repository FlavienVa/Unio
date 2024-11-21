package com.android.unio.ui.user

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.android.unio.R
import com.android.unio.mocks.user.MockUser
import com.android.unio.model.image.ImageRepository
import com.android.unio.model.strings.StoragePathsStrings
import com.android.unio.model.strings.test_tags.UserSettingsTestTags
import com.android.unio.model.user.AccountDetailsError
import com.android.unio.model.user.Interest
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.android.unio.model.user.UserViewModel
import com.android.unio.model.user.checkNewUser
import com.android.unio.ui.authentication.overlay.InterestOverlay
import com.android.unio.ui.authentication.overlay.SocialOverlay
import com.android.unio.ui.components.InterestInputChip
import com.android.unio.ui.components.ProfilePicturePicker
import com.android.unio.ui.components.SocialInputChip
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import com.android.unio.ui.theme.AppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun UserProfileSettingsScreen(
    userViewModel: UserViewModel,
    imageRepository: ImageRepository,
    navigationAction: NavigationAction
){

    val context = LocalContext.current
    val userId = Firebase.auth.currentUser?.uid

    val user by userViewModel.user.collectAsState()

    if(userId != userViewModel.user.value!!.uid){
        println("DEBUG, NOT SUPPOSED TO BE HERE THEY MUST HAVE THE SAME UID")
    }

    var profilePicture: Uri
    imageRepository.getImageUrl(
        user!!.profilePicture,
        onSuccess = {string ->
            profilePicture = Uri.parse(string)
        },
        onFailure = {
            Toast.makeText(
                context,
                context.getString(R.string.account_details_image_upload_error),
                Toast.LENGTH_SHORT)
                .show()
        }
    )

    UserProfileSettingsScreenContent(
        user = user!!,
        onDiscardChanges = { navigationAction.goBack() },
        onModifyUser = { profilePictureUri, createUser ->
            if (profilePictureUri.value == Uri.EMPTY) {
                createUser("")
            } else {
                val inputStream = context.contentResolver.openInputStream(profilePictureUri.value)
                imageRepository.uploadImage(
                    inputStream!!,
                    StoragePathsStrings.USER_IMAGES + userId,
                    onSuccess = { createUser(StoragePathsStrings.USER_IMAGES + userId) },
                    onFailure = { exception ->
                        Log.e("AccountDetails", "Error uploading image: $exception")
                        Toast.makeText(
                            context,
                            context.getString(R.string.account_details_image_upload_error),
                            Toast.LENGTH_SHORT)
                            .show()
                    })
            }
        },
        onUploadUser = { modifiedUser ->
            userViewModel.addUser(
                modifiedUser,
                onSuccess = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.account_details_created_successfully),
                        Toast.LENGTH_SHORT)
                        .show()
                    navigationAction.navigateTo(Screen.MY_PROFILE)
                })
        }
    )

}


@Preview
@Composable
fun PreviewAccountSettings(

){
    val navController = rememberNavController()
    val navigationAction = NavigationAction(navController)


    val mockUser = MockUser.createMockUser()

    ProvidePreferenceLocals { AppTheme { UserProfileSettingsScreenContent(mockUser, {},
        onModifyUser =  { uri, method -> println() },
        onUploadUser = {user -> println() }) } }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSettingsScreenContent(
    user: User,
    onDiscardChanges: () -> Unit,
    onModifyUser: (MutableState<Uri>, (String) -> Unit) -> Unit,
    onUploadUser: (User) -> Unit,
){

    val context = LocalContext.current

    var firstName : String by remember { mutableStateOf(user.firstName) }
    var lastName : String by remember { mutableStateOf(user.lastName) }
    var bio : String by remember { mutableStateOf(user.biography) }

    var isErrors by remember { mutableStateOf(mutableSetOf<AccountDetailsError>()) }


    val userInterestsFlow =
        remember { MutableStateFlow(Interest.entries.map {it to mutableStateOf(user.interests.contains(it)) } ) }

    val userSocialsFlow =
        remember { MutableStateFlow(user.socials.toMutableList()) }

    val interests by userInterestsFlow.collectAsState()
    val socials by userSocialsFlow.collectAsState()


    val profilePictureUri = remember { mutableStateOf<Uri>(Uri.parse(user.profilePicture)) }

    var showInterestsOverlay by remember { mutableStateOf(false) }
    var showSocialsOverlay by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    /**
     * uid, email, followedAssociations, joinedAssociations and savedEvents
     * will not be modified and simply be copied from the user.
     */
    val modifyUser: (String) -> Unit = { uri ->
        val newUser =
            User(
                uid = user.uid,
                email = user.email,
                firstName = firstName,
                lastName = lastName,
                biography = bio,
                followedAssociations = user.followedAssociations,
                joinedAssociations = user.joinedAssociations,
                savedEvents = user.savedEvents,
                interests = interests.filter { it.second.value }.map { it.first },
                socials = socials,
                profilePicture = uri)

        isErrors = checkNewUser(newUser)
        if (isErrors.isEmpty()) {
            onUploadUser(newUser)
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.user_settings_discard_changes)) },
                navigationIcon = {
                    IconButton(
                        onClick = onDiscardChanges,
                    ){
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back arrow"
                        )
                    }
                })
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(40.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            ProfilePicturePicker(profilePictureUri, { profilePictureUri.value = Uri.EMPTY })

            EditUserTextFields(
                isErrors = isErrors,
                firstName = firstName,
                lastName = lastName,
                bio = bio,
                onFirstNameChange = {firstName = it},
                onLastNameChange = {lastName = it},
                onBioChange = {bio = it},
            )

            SocialButtonAndFlowRow(
                userSocialFlow = userSocialsFlow,
                onShowSocials = { showSocialsOverlay = true }
            )

            InterestButtonAndFlowRow(
                interestsFlow = userInterestsFlow,
                onShowInterests = { showInterestsOverlay = true }
            )


            Button(
                onClick = { onModifyUser(profilePictureUri, modifyUser) },
                modifier = Modifier.testTag(UserSettingsTestTags.SAVE_BUTTON)
            ) {
                Text(context.getString(R.string.user_settings_save_changes))
            }


        }

        if (showInterestsOverlay) {
            InterestOverlay(
                onDismiss = { showInterestsOverlay = false },
                onSave = { newInterests ->
                    userInterestsFlow.value = newInterests
                    showInterestsOverlay = false
                },
                interests = interests)
        }

        if (showSocialsOverlay) {
            SocialOverlay(
                onDismiss = { showSocialsOverlay = false },
                onSave = { newUserSocials ->
                    userSocialsFlow.value = newUserSocials
                    showSocialsOverlay = false
                },
                userSocials = socials)
        }
    }

}

@Composable
private fun EditUserTextFields(
    isErrors: MutableSet<AccountDetailsError>,
    firstName: String,
    lastName: String,
    bio: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit
) {
    val context = LocalContext.current
    val isFirstNameError = isErrors.contains(AccountDetailsError.EMPTY_FIRST_NAME)
    val isLastNameError = isErrors.contains(AccountDetailsError.EMPTY_LAST_NAME)

    OutlinedTextField(
        modifier =
        Modifier
            .padding(4.dp)
            .testTag(UserSettingsTestTags.FIRST_NAME_TEXT_FIELD),
        label = {
            Text(
                context.getString(R.string.user_settings_first_name),
                modifier = Modifier.testTag(UserSettingsTestTags.FIRST_NAME_TEXT))
        },
        isError = (isFirstNameError),
        supportingText = {
            if (isFirstNameError) {
                Text(
                    context.getString(AccountDetailsError.EMPTY_FIRST_NAME.errorMessage),
                    modifier = Modifier.testTag(UserSettingsTestTags.FIRST_NAME_ERROR_TEXT))
            }
        },
        onValueChange = onFirstNameChange,
        value = firstName)

    OutlinedTextField(
        modifier =
        Modifier
            .padding(4.dp)
            .testTag(UserSettingsTestTags.LAST_NAME_TEXT_FIELD),
        label = {
            Text(
                context.getString(R.string.user_settings_last_name),
                modifier = Modifier.testTag(UserSettingsTestTags.LAST_NAME_TEXT))
        },
        isError = (isLastNameError),
        supportingText = {
            if (isLastNameError) {
                Text(
                    context.getString(AccountDetailsError.EMPTY_LAST_NAME.errorMessage),
                    modifier = Modifier.testTag(UserSettingsTestTags.LAST_NAME_ERROR_TEXT))
            }
        },
        onValueChange = onLastNameChange,
        value = lastName)

    OutlinedTextField(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(200.dp)
            .testTag(UserSettingsTestTags.BIOGRAPHY_TEXT_FIELD),
        label = {
            Text(
                context.getString(R.string.user_settings_bio),
                modifier = Modifier.testTag(UserSettingsTestTags.BIOGRAPHY_TEXT))
        },
        onValueChange = onBioChange,
        value = bio)
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestButtonAndFlowRow(
    interestsFlow: MutableStateFlow<List<Pair<Interest, MutableState<Boolean>>>>,
    onShowInterests: () -> Unit,
) {
    val context = LocalContext.current

    val interests by interestsFlow.collectAsState()

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(UserSettingsTestTags.INTERESTS_BUTTON),
        onClick = onShowInterests) {
        Icon(
            Icons.Default.Add,
            contentDescription =
            context.getString(R.string.account_details_content_description_add))
        Text(context.getString(R.string.user_settings_edit_interests))
    }
    FlowRow {
        interests.forEachIndexed { index, pair ->
            if (pair.second.value) {
                InterestInputChip(pair,index, testTag = UserSettingsTestTags.INTERESTS_CHIP)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SocialButtonAndFlowRow(
    userSocialFlow: MutableStateFlow<MutableList<UserSocial>>,
    onShowSocials: () -> Unit,
){

    val context = LocalContext.current
    val socials by userSocialFlow.collectAsState()

    OutlinedButton(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(UserSettingsTestTags.SOCIALS_BUTTON),
        onClick = onShowSocials) {
        Icon(
            Icons.Default.Add,
            contentDescription =
            context.getString(R.string.account_details_content_description_close))
        Text(context.getString(R.string.user_settings_edit_socials))
    }

    FlowRow(modifier = Modifier
        .fillMaxWidth()
        .padding(3.dp)) {
        socials.forEachIndexed { index, userSocial ->
            SocialInputChip(userSocial,
                onRemove = {userSocialFlow.value = userSocialFlow.value.toMutableList().apply { removeAt(index) }},
                testTag = UserSettingsTestTags.SOCIALS_CHIP)
        }
    }

}