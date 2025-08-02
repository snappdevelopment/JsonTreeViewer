package com.sebastianneubauer.jsontreeviewer

import com.sebastianneubauer.jsontreeviewer.Contract.State
import com.sebastianneubauer.jsontreeviewer.ui.DragAndDropState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val json = "{\"Hello\": \"World\"}"

    private val underTest = ViewModel(
        coroutineScope = TestScope(dispatcher),
        ioDispatcher = dispatcher,
    )

    private val contentState = State.Content(
        json = json,
        searchDirection = null,
        displayMode = Contract.DisplayMode.Render
    )

    private fun getFile(): File {
        return Files.createTempFile("file", ".json").toFile().also { it.writeText(json) }
    }

    @Test
    fun `initial state is Initial`() {
        assertEquals(
            expected = State.Initial,
            actual = underTest.state.value
        )
    }

    @Test
    fun `parsing valid file returns Content state`() {
        val file = getFile()
        underTest.updateDragAndDropState(DragAndDropState.Success(file))

        assertEquals(
            expected = contentState,
            actual = underTest.state.value
        )
    }

    @Test
    fun `parsing non-existent file returns Error state`() {
        val file = File("non-existent")
        underTest.updateDragAndDropState(DragAndDropState.Success(file))

        assertEquals(
            expected = State.Error(error = Contract.ErrorType.FileReadError),
            actual = underTest.state.value
        )
    }

    @Test
    fun `parsing directory returns Error state`() {
        val dir = Files.createTempDirectory("dir").toFile()
        underTest.updateDragAndDropState(DragAndDropState.Success(dir))

        assertEquals(
            expected = State.Error(error = Contract.ErrorType.FileReadError),
            actual = underTest.state.value
        )
    }

    @Test
    fun `parsing unsupported file returns Error state`() {
        val file = Files.createTempFile("file", ".kt").toFile().also { it.writeText(json) }
        underTest.updateDragAndDropState(DragAndDropState.Success(file))

        assertEquals(
            expected = State.Error(error = Contract.ErrorType.FileReadError),
            actual = underTest.state.value
        )
    }

    @Test
    fun `drop event does not contain file returns Error state`() {
        underTest.updateDragAndDropState(DragAndDropState.Failure)

        assertEquals(
            expected = State.Error(error = Contract.ErrorType.DataDragAndDropError),
            actual = underTest.state.value
        )
    }

    @Test
    fun `updateDisplayMode updates the Content state with the new DisplayMode`() {
        // Arrange: Start in Content state with Render mode
        val file = getFile()
        underTest.updateDragAndDropState(DragAndDropState.Success(file))
        assertEquals(
            expected = contentState,
            actual = underTest.state.value
        )

        underTest.updateDisplayMode(Contract.DisplayMode.Edit)

        assertEquals(
            expected = contentState.copy(
                displayMode = Contract.DisplayMode.Edit
            ),
            actual = underTest.state.value
        )
    }

    @Test
    fun `updateJson updates the Content state with the new json`() {
        // Arrange: Start in Content state with Render mode
        val file = getFile()
        underTest.updateDragAndDropState(DragAndDropState.Success(file))
        assertEquals(
            expected = contentState,
            actual = underTest.state.value
        )

        underTest.updateJson("hello")

        assertEquals(
            expected = contentState.copy(json = "hello"),
            actual = underTest.state.value
        )
    }

    @Test
    fun `reset updates the state to the initial state`() {
        // Arrange: Start in Content state with Render mode
        val file = getFile()
        underTest.updateDragAndDropState(DragAndDropState.Success(file))
        assertEquals(
            expected = contentState,
            actual = underTest.state.value
        )

        underTest.reset()

        assertEquals(
            expected = State.Initial,
            actual = underTest.state.value
        )
    }

}