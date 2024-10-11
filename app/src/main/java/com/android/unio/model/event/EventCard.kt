package com.android.unio.model.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.unio.R

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    val backgroundColor = Color(0xFF2596BE)
    val backgroundImage = painterResource(id = R.drawable.photo_2024_10_08_14_57_48)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
            .testTag("event_EventListItem")
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF0ECF4))
    ) {

        Image(
            painter = DynamicImage(event.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .testTag("event_EventImage"),
            contentScale = ContentScale.Crop // crop the image to fit
        )


        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        modifier = Modifier
                            .padding(vertical = 1.dp, horizontal = 4.dp)
                            .testTag("event_EventTitle")
                            .wrapContentWidth(), // Make sure the text only takes as much space as needed
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(6.dp))


                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(addAlphaToColor(event.types.get(0).color, 200))
                            .wrapContentWidth()
                    ) {
                        Text(
                            text = event.types.get(0).text,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .testTag("event_EventMainType"),
                            color = Color.Black,
                            style = TextStyle(fontSize = 8.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))


                Image(
                    painter = painterResource(id = R.drawable.clic),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                        .testTag("event_ClicImage")
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        modifier = Modifier
                            .padding(vertical = 1.dp, horizontal = 4.dp)
                            .testTag("event_EventLocation")
                            .wrapContentWidth(), // Make sure the text only takes as much space as needed
                        text = event.location.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                }


                Spacer(modifier = Modifier.width(1.dp))

                Text(
                    modifier = Modifier
                        .padding(vertical = 1.dp, horizontal = 4.dp)
                        .testTag("event_EventDate")
                        .wrapContentWidth(),
                    text = formatTimestampToMMDD(event.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(1.dp))

                Text(
                    modifier = Modifier
                        .testTag("event_EventTime")
                        .wrapContentWidth(),
                    text = formatTimestampToHHMM(event.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )


            }

            Text(
                modifier = Modifier
                    .testTag("event_EventCatchyDescription")
                    .wrapContentWidth(),
                text = event.catchyDescription,
                style = TextStyle(fontSize = 12.sp),
                color = Color.Black
            )


        }
    }
}
