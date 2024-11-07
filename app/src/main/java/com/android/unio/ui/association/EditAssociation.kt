package com.android.unio.ui.association

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.android.unio.R
import com.android.unio.model.association.AssociationCategory
import com.android.unio.model.association.AssociationViewModel
import com.android.unio.model.image.ImageRepository
import com.android.unio.ui.navigation.NavigationAction
import com.android.unio.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun EditAssociationScreen(
    associationId: String,
    associationViewModel: AssociationViewModel,
    imageRepository: ImageRepository,
    navigationAction: NavigationAction,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val associations by associationViewModel.associations.collectAsState()
  val association = associations.find { it.uid == associationId }

  if (association == null) {
    Text(
        text = context.getString(R.string.association_not_found),
        color = Color.Red,
        modifier = Modifier.padding(16.dp).testTag("AssociationNotFoundText"))
  } else {
    var url by remember { mutableStateOf(TextFieldValue(association.url)) }
    var name by remember { mutableStateOf(TextFieldValue(association.name)) }
    var fullName by remember { mutableStateOf(TextFieldValue(association.fullName)) }
    var description by remember { mutableStateOf(TextFieldValue(association.description)) }
    var image by remember { mutableStateOf(TextFieldValue(association.image)) }

    var expanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(association.category) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
      Text(
          text = context.getString(R.string.edit_association_title),
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.testTag("EditAssociationTitle"))

      Spacer(modifier = Modifier.height(16.dp))

      // Explanation for "Name"
      Text(
          text = context.getString(R.string.name_explanation),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag("NameExplanationText"))

      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Name") },
          modifier = Modifier.fillMaxWidth().testTag("NameTextField"))

      Spacer(modifier = Modifier.height(16.dp))

      // Explanation for "Full Name"
      Text(
          text = context.getString(R.string.full_name_explanation),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag("FullNameExplanationText"))

      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("FullNameTextField"))

      Spacer(modifier = Modifier.height(16.dp))

      // Explanation for "Category"
      Text(
          text = context.getString(R.string.category_explanation),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag("CategoryExplanationText"))

      Spacer(modifier = Modifier.height(8.dp))

      // Category Button
      Button(
          onClick = { expanded = true },
          modifier = Modifier.fillMaxWidth().testTag("CategoryButton")) {
            Text(text = category.displayName)
          }

      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        AssociationCategory.values().forEach { categoryOption ->
          DropdownMenuItem(
              text = { Text(text = categoryOption.displayName) },
              onClick = {
                category = categoryOption
                expanded = false
              })
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Explanation for "Description"
      Text(
          text = context.getString(R.string.description_explanation),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag("DescriptionExplanationText"))

      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          modifier = Modifier.fillMaxWidth().testTag("DescriptionTextField"))

      Spacer(modifier = Modifier.height(16.dp))

      // Explanation for "Image URL"
      Text(
          text = context.getString(R.string.image_explanation),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag("ImageExplanationText"))

      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
          value = image,
          onValueChange = { image = it },
          label = { Text("Image URL") },
          modifier = Modifier.fillMaxWidth().testTag("ImageTextField"))

      Spacer(modifier = Modifier.height(16.dp))

      // Explanation for "URL"
      Text(
          text = context.getString(R.string.url_explanation),
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag("UrlExplanationText"))

      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text("URL") },
          modifier = Modifier.fillMaxWidth().testTag("UrlTextField"))

      Spacer(modifier = Modifier.height(24.dp))

      Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = {
                  navigationAction.navigateTo(
                      Screen.withParams(Screen.ASSOCIATION_PROFILE, association.uid))
                },
                modifier = Modifier.testTag("CancelButton")) {
                  Text(context.getString(R.string.cancel_button_text))
                }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                  scope.launch {
                    associationViewModel.saveAssociation(
                        association.copy(
                            name = name.text,
                            fullName = fullName.text,
                            description = description.text,
                            image = image.text,
                            category = category,
                            url = url.text),
                        null,
                        onSuccess = {
                          Log.d("EditAssociationScreen", "Association saved successfully.")
                          navigationAction.navigateTo(
                              Screen.withParams(Screen.ASSOCIATION_PROFILE, association.uid),
                              Screen.EDIT_ASSOCIATION)
                        },
                        onFailure = {
                          Log.e("EditAssociationScreen", "Failed to save association.")
                          Toast.makeText(
                                  context,
                                  context.getString(R.string.save_failed_message),
                                  Toast.LENGTH_SHORT)
                              .show()
                        })
                  }
                },
                modifier = Modifier.testTag("SaveButton")) {
                  Text(context.getString(R.string.save_button_text))
                }
          }
    }
  }
}
