package com.example.agent.permission

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Permission(
    val name: String,
    val description: String,
    var isGranted: Boolean
)

@Composable
fun PermissionItem(
    permission: Permission,
    onRequestPermission: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = permission.description) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onRequestPermission() },
        trailingContent = { Checkbox(checked = permission.isGranted, onCheckedChange = {
            onRequestPermission()
        }) }
    )
}