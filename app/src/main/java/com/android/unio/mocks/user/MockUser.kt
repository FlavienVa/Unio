package com.android.unio.mocks.user

import com.android.unio.mocks.association.MockAssociation
import com.android.unio.mocks.event.MockEvent
import com.android.unio.mocks.firestore.MockReferenceList
import com.android.unio.model.association.Association
import com.android.unio.model.event.Event
import com.android.unio.model.user.Interest
import com.android.unio.model.user.Social
import com.android.unio.model.user.User
import com.android.unio.model.user.UserSocial
import com.google.firebase.Timestamp

/** MockUser class provides edge-case instances of the User data class for testing purposes. */
class MockUser {
    companion object {

        /** Enums for each edge-case category **/
        enum class EdgeCaseUid(val value: String) {
            EMPTY(""),
            SPECIAL_CHARACTERS("user#@$!"),
            LONG("user-very-long-id-1234567890123456789012345678901234567890"),
            TYPICAL("user123")
        }

        enum class EdgeCaseEmail(val value: String) {
            EMPTY(""),
            VALID("user@example.com"),
            LONG("user.with.a.very.long.email@exampledomainwithaverylongdomainname.com"),
            INVALID("user@no-tld")
        }

        enum class EdgeCaseFirstName(val value: String) {
            EMPTY(""),
            SHORT("A"),
            LONG("FirstnameWithQuiteALotOfCharacters"),
            SPECIAL_CHARACTERS("FïrstNäme")
        }

        enum class EdgeCaseLastName(val value: String) {
            EMPTY(""),
            SHORT("B"),
            LONG("LastnameWithManyCharactersForTesting"),
            SPECIAL_CHARACTERS("LàstÑäme")
        }

        enum class EdgeCaseBiography(val value: String) {
            EMPTY(""),
            SHORT("Bio text."),
            LONG("This is a very long biography meant to test the handling of large text fields. ".repeat(10)),
            SPECIAL_CHARACTERS("Biography with special characters #, @, $, % and accents é, ü, ñ.")
        }

        enum class EdgeCaseProfilePicture(val value: String) {
            EMPTY(""),
            VALID("https://example.com/profile.png"),
            LONG("https://example.com/very/long/path/to/profile/image/that/may/exceed/limits/image.png"),
            INVALID("invalid-url")
        }

        /** Edge cases for Interests and Socials */
        val edgeCaseInterests = Interest.values().toList()
        val edgeCaseSocials = Social.values().map { social ->
            when (social) {
                Social.WHATSAPP -> UserSocial(Social.WHATSAPP, "1234567890")
                else -> UserSocial(social, "user123")
            }
        }

        /** Returns a list of edge-case users based on selected edge cases */
        fun createEdgeCaseMockUsers(
            selectedUids: List<EdgeCaseUid> = EdgeCaseUid.values().toList(),
            selectedEmails: List<EdgeCaseEmail> = EdgeCaseEmail.values().toList(),
            selectedFirstNames: List<EdgeCaseFirstName> = EdgeCaseFirstName.values().toList(),
            selectedLastNames: List<EdgeCaseLastName> = EdgeCaseLastName.values().toList(),
            selectedBiographies: List<EdgeCaseBiography> = EdgeCaseBiography.values().toList(),
            selectedProfilePictures: List<EdgeCaseProfilePicture> = EdgeCaseProfilePicture.values().toList(),
            selectedInterests: List<Interest> = edgeCaseInterests,
            selectedSocials: List<UserSocial> = edgeCaseSocials,
            selectedFollowedAssociations: List<List<Association>> = listOf(MockAssociation.createAllMockAssociations()),
            selectedJoinedAssociations: List<List<Association>> = listOf(MockAssociation.createAllMockAssociations()),
            selectedSavedEvents: List<List<Event>> = listOf(MockEvent.createAllMockEvents())
        ): List<User> {
            val users = mutableListOf<User>()
            for (uid in selectedUids) {
                for (email in selectedEmails) {
                    for (firstName in selectedFirstNames) {
                        for (lastName in selectedLastNames) {
                            for (biography in selectedBiographies) {
                                for (profilePicture in selectedProfilePictures) {
                                    for (interests in listOf(selectedInterests)) {
                                        for (socials in listOf(selectedSocials)) {
                                            for (followedAssociations in selectedFollowedAssociations) {
                                                for (joinedAssociations in selectedJoinedAssociations) {
                                                    for (savedEvents in selectedSavedEvents) {
                                                        users.add(
                                                            createMockUser(
                                                                uid = uid.value,
                                                                email = email.value,
                                                                firstName = firstName.value,
                                                                lastName = lastName.value,
                                                                biography = biography.value,
                                                                profilePicture = profilePicture.value,
                                                                interests = interests,
                                                                socials = socials,
                                                                followedAssociations = followedAssociations,
                                                                joinedAssociations = joinedAssociations,
                                                                savedEvents = savedEvents
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return users
        }

        /** Creates a mock User with specified properties for testing edge cases. */
        fun createMockUser(
            associationDependency: Boolean = false,
            eventDependency: Boolean = false,
            uid: String = "user123",
            email: String = "user@example.com",
            firstName: String = "John",
            lastName: String = "Doe",
            biography: String = "This is a sample biography.",
            followedAssociations: List<Association> =
                if (associationDependency) {
                    emptyList()
                } else {
                    MockAssociation.createAllMockAssociations(eventDependency = eventDependency, userDependency = true)
                }, // avoid circular dependency
            joinedAssociations: List<Association> =
                if (associationDependency) {
                    emptyList()
                } else {
                    MockAssociation.createAllMockAssociations(eventDependency = eventDependency, userDependency = true)
                }, // avoid circular dependency
            savedEvents: List<Event> = if(eventDependency) {
                emptyList()
            } else {
                MockEvent.createAllMockEvents(associationDependency = associationDependency)
            },
            interests: List<Interest> = listOf(Interest.SPORTS),
            socials: List<UserSocial> = listOf(UserSocial(Social.INSTAGRAM, "user123")),
            profilePicture: String = "https://example.com/profile.png"
        ): User {
            return User(
                uid = uid,
                email = email,
                firstName = firstName,
                lastName = lastName,
                biography = biography,
                followedAssociations = MockReferenceList(followedAssociations),
                joinedAssociations = MockReferenceList(joinedAssociations),
                savedEvents = MockReferenceList(savedEvents),
                interests = interests,
                socials = socials,
                profilePicture = profilePicture
            )
        }

        /** Creates a list of mock Users with default properties */
        fun createAllMockUsers(
            size: Int = 5,
            associationDependency: Boolean = false,
            eventDependency: Boolean = false,
        ): List<User> {
            return List(size) { createMockUser(associationDependency = associationDependency, eventDependency = eventDependency) }
        }
    }
}
