package com.strela.onebox.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.strela.onebox.i18n.strings
import com.strela.onebox.profile.ImportContent
import com.strela.onebox.profile.ImportDetector
import com.strela.onebox.profile.Profile
import com.strela.onebox.profile.Server
import com.strela.onebox.ui.design.Badge
import com.strela.onebox.ui.design.Icon
import com.strela.onebox.ui.design.IconButton
import com.strela.onebox.ui.design.IconTile
import com.strela.onebox.ui.design.LocalOneBoxTypography
import com.strela.onebox.ui.design.Palette
import com.strela.onebox.ui.design.Panel
import com.strela.onebox.ui.design.PrimaryButton
import com.strela.onebox.ui.design.SectionLabel
import com.strela.onebox.ui.design.TextField
import com.strela.onebox.ui.design.icons.OneBoxIcons
import com.strela.onebox.xray.XrayController

/**
 * Экран «Server»: единый импорт (ссылки / JSON / base64) и полный список профилей
 * со всеми их серверами. Формат вставленного текста определяется автоматически.
 */
@Composable
fun ServersScreen(
    profiles: List<Profile>,
    selectedProfileId: String?,
    pings: Map<String, XrayController.Ping>,
    onImportServers: (servers: List<Server>, profileName: String) -> Int,
    onImportConfig: (json: String) -> Unit,
    onSelectProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onSelectServer: (profileId: String, serverId: String) -> Unit,
    onDeleteServer: (profileId: String, serverId: String) -> Unit,
    onPing: (Server) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by rememberSaveable { mutableStateOf("") }
    var profileName by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<ImportMessage?>(null) }
    val s = strings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        ImportPanel(
            input = input,
            onInputChange = {
                input = it
                message = null
            },
            profileName = profileName,
            onProfileNameChange = { profileName = it },
            message = message,
            onImport = {
                when (val content = ImportDetector.detect(input)) {
                    is ImportContent.Servers -> {
                        val added = onImportServers(content.servers, profileName)
                        message = if (added > 0) {
                            ImportMessage(s.addedServers(added), ImportMessage.Tone.SUCCESS)
                        } else {
                            ImportMessage(s.noNewServers, ImportMessage.Tone.ERROR)
                        }
                        if (added > 0) {
                            input = ""
                            profileName = ""
                        }
                    }

                    is ImportContent.Config -> {
                        onImportConfig(content.json)
                        message = ImportMessage(s.configImported, ImportMessage.Tone.SUCCESS)
                        input = ""
                        profileName = ""
                    }

                    null -> message = ImportMessage(
                        text = if (input.isBlank()) s.pasteLinkOrLines else s.unrecognizedInput,
                        tone = if (input.isBlank()) ImportMessage.Tone.INFO
                        else ImportMessage.Tone.ERROR
                    )
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        if (profiles.isEmpty()) {
            EmptyProfiles()
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(
                    text = s.allProfilesAndServers,
                    icon = OneBoxIcons.Folder
                )
                Badge(
                    text = s.profilesServersBadge(
                        profiles.size,
                        profiles.sumOf { it.servers.size }
                    ),
                    color = Palette.TextSecondary,
                    background = Palette.SurfaceElevated
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                profiles.forEach { profile ->
                    ProfileCard(
                        profile = profile,
                        selected = profile.id == selectedProfileId,
                        pings = pings,
                        onSelectProfile = { onSelectProfile(profile.id) },
                        onDeleteProfile = { onDeleteProfile(profile.id) },
                        onSelectServer = { server -> onSelectServer(profile.id, server.id) },
                        onDeleteServer = { server -> onDeleteServer(profile.id, server.id) },
                        onPing = onPing
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ImportPanel(
    input: String,
    onInputChange: (String) -> Unit,
    profileName: String,
    onProfileNameChange: (String) -> Unit,
    message: ImportMessage?,
    onImport: () -> Unit
) {
    val typo = LocalOneBoxTypography.current
    val s = strings

    Panel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(
                    icon = OneBoxIcons.Link,
                    size = 38.dp,
                    iconSize = 19.dp,
                    tint = Palette.AccentPrimary,
                    background = Palette.AccentSoft
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    BasicText(
                        text = s.addServers,
                        style = typo.subtitle.copy(color = Palette.TextPrimary)
                    )
                    BasicText(
                        text = "URI · JSON · base64",
                        style = typo.caption.copy(color = Palette.TextMuted),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            TextField(
                value = profileName,
                onValueChange = onProfileNameChange,
                label = s.profileName,
                placeholder = s.profileNamePlaceholder,
                mono = false,
                leadingIcon = OneBoxIcons.Tag,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            TextField(
                value = input,
                onValueChange = onInputChange,
                label = "",
                placeholder = "vless://…\nvmess://…\nss://…\n{ \"outbounds\": [ … ] }\nbase64…",
                minLines = 4,
                leadingIcon = OneBoxIcons.Link,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryButton(
                    text = s.importAction,
                    icon = OneBoxIcons.Plus,
                    onClick = onImport,
                    enabled = input.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
                if (input.isNotBlank()) {
                    IconButton(
                        icon = OneBoxIcons.Close,
                        contentDescription = s.clearField,
                        onClick = { onInputChange("") },
                        tint = Palette.TextSecondary,
                        background = Palette.SurfaceElevated
                    )
                }
            }

            message?.let { msg ->
                Spacer(Modifier.height(10.dp))
                InlineMessage(message = msg)
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    selected: Boolean,
    pings: Map<String, XrayController.Ping>,
    onSelectProfile: () -> Unit,
    onDeleteProfile: () -> Unit,
    onSelectServer: (Server) -> Unit,
    onDeleteServer: (Server) -> Unit,
    onPing: (Server) -> Unit
) {
    val typo = LocalOneBoxTypography.current
    val s = strings
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Palette.Surface)
            .border(BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) Palette.AccentPrimary else Palette.Border), shape)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onSelectProfile)
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(
                icon = if (selected) OneBoxIcons.Check else OneBoxIcons.Folder,
                size = 36.dp,
                iconSize = 18.dp,
                tint = if (selected) Palette.AccentPrimary else Palette.TextSecondary,
                background = if (selected) Palette.AccentSoft else Palette.SurfaceElevated,
                corner = 10.dp
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp, end = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(
                        text = profile.name,
                        style = typo.subtitle.copy(color = Palette.TextPrimary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (selected) {
                        Badge(
                            text = s.active,
                            color = Palette.TextPrimary,
                            background = Palette.AccentSoft,
                            modifier = Modifier.padding(start = 7.dp)
                        )
                    }
                }
                val subtitle = buildString {
                    append(s.serversCount(profile.servers.size))
                    profile.selectedServer?.let { append(" · ").append(it.endpoint) }
                }
                BasicText(
                    text = subtitle,
                    style = typo.caption.copy(color = Palette.TextMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            IconButton(
                icon = OneBoxIcons.Trash,
                contentDescription = s.deleteProfile(profile.name),
                onClick = onDeleteProfile,
                tint = Palette.TextMuted,
                size = 32.dp,
                iconSize = 16.dp
            )
        }

        if (profile.servers.isEmpty()) {
            BasicText(
                text = s.noServersInProfile,
                style = typo.caption.copy(color = Palette.TextMuted),
                modifier = Modifier.padding(top = 8.dp, start = 2.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                profile.servers.forEach { server ->
                    ServerRow(
                        server = server,
                        selected = selected && server.id == profile.selectedServer?.id,
                        ping = pings[server.id],
                        onSelect = { onSelectServer(server) },
                        onPing = { onPing(server) },
                        onDelete = { onDeleteServer(server) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProfiles() {
    val typo = LocalOneBoxTypography.current
    val s = strings
    Panel(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(vertical = 26.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconTile(
                icon = OneBoxIcons.Folder,
                size = 52.dp,
                iconSize = 24.dp,
                tint = Palette.TextSecondary,
                background = Palette.SurfaceElevated,
                corner = 16.dp
            )
            BasicText(
                text = s.noProfiles,
                style = typo.subtitle.copy(color = Palette.TextPrimary),
                modifier = Modifier.padding(top = 12.dp)
            )
            BasicText(
                text = s.noProfilesHint,
                style = typo.body.copy(color = Palette.TextMuted, textAlign = TextAlign.Center),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Сообщение под кнопкой импорта. */
private data class ImportMessage(val text: String, val tone: Tone) {
    enum class Tone { SUCCESS, INFO, ERROR }
}

@Composable
private fun InlineMessage(message: ImportMessage) {
    val typo = LocalOneBoxTypography.current
    val (color, icon) = when (message.tone) {
        ImportMessage.Tone.SUCCESS -> Palette.Running to OneBoxIcons.Check
        ImportMessage.Tone.INFO -> Palette.TextSecondary to OneBoxIcons.Info
        ImportMessage.Tone.ERROR -> Palette.Error to OneBoxIcons.Alert
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon = icon,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        BasicText(
            text = message.text,
            style = typo.caption.copy(color = color),
            modifier = Modifier.padding(start = 7.dp)
        )
    }
}
