/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalHorologistApi::class)

package com.example.wear.tiles.messaging.tile

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.material.Button
import androidx.wear.protolayout.material.ButtonColors
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.MultiButtonLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.wear.tiles.R
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MessagingRepo.Companion.knownContacts
import com.example.wear.tiles.messaging.tile.MessagingTileRenderer.Companion.ID_IC_SEARCH
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.images.toImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement {
        return messagingTileLayout(
            context = context,
            deviceParameters = deviceParameters,
            state = state,
            searchButtonClickable = launchActivityClickable("search_button", openSearch())
        )
    }

    override fun Resources.Builder.produceRequestedResources(
        resourceState: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: List<String>
    ) {
        addIdToImageMapping(ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24))

        resourceState.forEach { (contact, bitmap) ->
            addIdToImageMapping(
                /* id = */ contact.imageResourceId(),
                /* image = */ bitmap.toImageResource()
            )
        }
    }

    companion object {
        internal const val ID_IC_SEARCH = "ic_search"
    }

}

/**
 * Layout definition for the Messaging Tile.
 */
private fun messagingTileLayout(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    state: MessagingTileState,
    searchButtonClickable: ModifiersBuilders.Clickable
) = PrimaryLayout.Builder(deviceParameters)
    .setResponsiveContentInsetEnabled(true)
    .setContent(
        MultiButtonLayout.Builder()
            .apply {
                state.contacts.take(4).forEach { contact ->
                    addButtonContent(
                        contactLayout(
                            context = context,
                            contact = contact,
                            clickable = emptyClickable
                        )
                    )
                }
            }
            .addButtonContent(searchLayout(context, searchButtonClickable))
            .build()
        /*
        Text.Builder(context, context.getString(R.string.hello_tile_body))
            .setTypography(Typography.TYPOGRAPHY_BODY1)
            .setColor(ColorBuilders.argb(Color.White.toArgb()))
            .build()
         */
    )
    .setPrimaryChipContent(
        CompactChip.Builder(
            context,
            context.getString(R.string.tile_messaging_create_new),
            emptyClickable,
            deviceParameters
        )
            .setChipColors(ChipColors.primaryChipColors(MessagingTileTheme.colors))
            .build()
    )
    .build()

/*
Other than the pretty obvious configurations is doing for the Button, the only magic here is the
context is passing Context. This is to have access to application resources like text the .xml and
icons from drawable, system services like location, conectivity and whatnot.
There's other things like Activity context vs Application context that I don't really understand.
 */
private fun searchLayout(
    context: Context,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(context.getString(R.string.tile_messaging_search))
    .setIconContent(MessagingTileRenderer.ID_IC_SEARCH)
    .setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
    .build()

// Just read, its pretty descriptive of what its doing
private fun contactLayout(
    context: Context,
    contact: Contact,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(contact.name)
    .apply {
        if (contact.avatarUrl != null) {
            setImageContent(contact.imageResourceId())
        } else {
            setTextContent(contact.initials)
            setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
        }
    }
    .build()

private fun previewResources() = Resources.Builder()
    .addIdToImageMapping(ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24))
    .addIdToImageMapping(knownContacts[1].imageResourceId(), drawableResToImageResource(R.drawable.ali))
    .addIdToImageMapping(knownContacts[2].imageResourceId(), drawableResToImageResource(R.drawable.taylor))
    .build()

/*
This rendering the tile only on in Android Studio, so that you don't need to wait the build every
time you change something on the layout
 */
@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun messagingTileLayoutPreview(context: Context): TilePreviewData {
    return TilePreviewData({ previewResources() }) { request ->
        MessagingTileRenderer(context).renderTimeline(
            MessagingTileState(knownContacts),
            request
        )
    }
}