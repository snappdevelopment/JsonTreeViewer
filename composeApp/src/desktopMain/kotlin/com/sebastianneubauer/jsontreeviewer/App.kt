package com.sebastianneubauer.jsontreeviewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.search.rememberSearchState
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropBox
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import com.sebastianneubauer.jsontreeviewer.ui.rememberDragAndDropTarget
import jsontreeviewer.composeapp.generated.resources.Res
import jsontreeviewer.composeapp.generated.resources.display_mode_edit
import jsontreeviewer.composeapp.generated.resources.display_mode_render
import jsontreeviewer.composeapp.generated.resources.error_copy_paste_read
import jsontreeviewer.composeapp.generated.resources.error_drag_and_drop
import jsontreeviewer.composeapp.generated.resources.error_file_read
import jsontreeviewer.composeapp.generated.resources.error_json_parser
import jsontreeviewer.composeapp.generated.resources.initial_drag_and_drop
import jsontreeviewer.composeapp.generated.resources.plus_circle_outline
import jsontreeviewer.composeapp.generated.resources.search_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(viewModel: ViewModel) {
    AppUi(
        state = viewModel.state.value,
        onDragAndDropStateChanged = viewModel::updateDragAndDropState,
        onDisplayModeChanged = viewModel::updateDisplayMode,
        onJsonChanged = viewModel::updateJson,
        onClearClicked = viewModel::reset,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppUi(
    state: Contract.State,
    onDragAndDropStateChanged: (DragAndDropState) -> Unit,
    onDisplayModeChanged: (Contract.DisplayMode) -> Unit,
    onJsonChanged: (String) -> Unit,
    onClearClicked: () -> Unit
) {
    var isHovering by remember { mutableStateOf(false) }

    val dragAndDropTarget = rememberDragAndDropTarget(
        onDragAndDropStateChanged = onDragAndDropStateChanged,
        onHoveringChanged = { isHovering = it }
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dragAndDropTarget
                )
        ) {
            when(state) {
                is Contract.State.Initial -> Initial(isHovering = isHovering)
                is Contract.State.InitialLoading -> InitialLoading(isHovering = isHovering)
                is Contract.State.Loading -> Loading()
                is Contract.State.Error -> Error(error = state.error, isHovering = isHovering)
                is Contract.State.Content -> Content(
                    json = state.json,
                    searchDirection = state.searchDirection,
                    displayMode = state.displayMode,
                    onDisplayModeChanged = onDisplayModeChanged,
                    onJsonChanged = onJsonChanged,
                    onClearClicked = onClearClicked
                )
            }
        }
    }
}

@Composable
private fun Content(
    json: String,
    searchDirection: Contract.SearchDirection?,
    displayMode: Contract.DisplayMode,
    onDisplayModeChanged: (Contract.DisplayMode) -> Unit,
    onJsonChanged: (String) -> Unit,
    onClearClicked: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val searchState = rememberSearchState()
    val listState = rememberLazyListState()
    val searchQuery by remember(searchState.query) { mutableStateOf(searchState.query.orEmpty()) }
    var jsonState by remember(json) { mutableStateOf(json) }

    LaunchedEffect(searchDirection) {
        when(searchDirection) {
            is Contract.SearchDirection.Next -> searchState.selectNext()
            is Contract.SearchDirection.Previous -> searchState.selectPrevious()
            null -> Unit
        }
    }

    val resultIndex = searchState.selectedResultListIndex
    LaunchedEffect(resultIndex) {
        if(resultIndex != null && !listState.isScrollInProgress) {
            listState.animateScrollToItem(resultIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Row {
            TextField(
                value = searchQuery,
                onValueChange = { searchState.query = it },
                singleLine = true,
                label = { Text(text = stringResource(Res.string.search_label)) },
                enabled = displayMode == Contract.DisplayMode.Render,
                colors = TextFieldDefaults.textFieldColors(
                    cursorColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                    focusedLabelColor = Color.Gray,
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    icon = Icons.Outlined.KeyboardArrowDown,
                    onClick = { coroutineScope.launch { searchState.selectNext() } }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    icon = Icons.Default.KeyboardArrowUp,
                    onClick = { coroutineScope.launch { searchState.selectPrevious() } }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${searchState.selectedResultIndex?.let { it + 1 } ?: 0}/${searchState.totalResults}",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1F))

                Button(
                    icon = Icons.Outlined.Close,
                    onClick = onClearClicked
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow {
            DisplayModeButton(
                isSelected = displayMode == Contract.DisplayMode.Render,
                onClick = { onDisplayModeChanged(Contract.DisplayMode.Render) },
                text = stringResource(Res.string.display_mode_render),
            )

            DisplayModeButton(
                isSelected = displayMode == Contract.DisplayMode.Edit,
                onClick = { onDisplayModeChanged(Contract.DisplayMode.Edit) },
                text = stringResource(Res.string.display_mode_edit),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            when(displayMode) {
                Contract.DisplayMode.Render -> {
                    var jsonErrorState by remember { mutableStateOf<Throwable?>(null) }
                    val throwable = jsonErrorState

                    if(throwable != null) {
                        Error(
                            title = stringResource(Res.string.error_json_parser),
                            message = throwable.localizedMessage
                        )
                    } else {
                        JsonTree(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = Color.White),
                            json = json,
                            searchState = searchState,
                            lazyListState = listState,
                            showIndices = true,
                            showItemCount = true,
                            expandSingleChildren = true,
                            onLoading = { Loading() },
                            onError = { jsonErrorState = it }
                        )
                    }
                }
                Contract.DisplayMode.Edit -> {
                    TextField(
                        modifier = Modifier.fillMaxSize(),
                        value = jsonState,
                        onValueChange = {
                            jsonState = it
                            onJsonChanged(it)
                        },
                        shape = RectangleShape,
                        colors = TextFieldDefaults.textFieldColors(
                            cursorColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = Color.Gray,
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun Initial(
    isHovering: Boolean,
) {
    DragAndDropBox(
        isHovering = isHovering,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = vectorResource(Res.drawable.plus_circle_outline),
                tint = Color.Gray,
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.initial_drag_and_drop),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun InitialLoading(
    isHovering: Boolean
) {
    DragAndDropBox(isHovering = isHovering) {
        CircularProgressIndicator(color = Color.Blue)
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = Color.Blue
        )
    }
}

@Composable
private fun Error(
    error: Contract.ErrorType,
    isHovering: Boolean
) {
    DragAndDropBox(isHovering = isHovering) {
        Error(
            title = when (error) {
                is Contract.ErrorType.DataDragAndDropError -> stringResource(Res.string.error_drag_and_drop)
                is Contract.ErrorType.FileReadError -> stringResource(Res.string.error_file_read)
                is Contract.ErrorType.CopyPasteError -> stringResource(Res.string.error_copy_paste_read)
            },
            message = null
        )
    }
}

@Composable
private fun Error(
    title: String,
    message: String?
) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        if(message != null) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun Button(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier
        .size(48.dp)
        .background(color = Color.LightGray.copy(alpha = 0.3F), shape = CircleShape),
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    }
}

@Composable
private fun SingleChoiceSegmentedButtonRowScope.DisplayModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    SegmentedButton(
        selected = isSelected,
        onClick = onClick,
        shape = RectangleShape,
        label = { Text(text = text) },
        icon = {},
        colors = SegmentedButtonDefaults.colors().copy(
            activeContainerColor = Color.LightGray.copy(alpha = 0.3F),
            inactiveContainerColor = Color.White,
            activeBorderColor = Color.Gray,
            inactiveBorderColor = Color.Gray
        )
    )
}