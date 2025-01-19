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
import kotlin.time.TestTimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val timeSource = TestTimeSource()
    private val json = "{\"Hello\": \"World\"}"

    private val underTest = ViewModel(
        coroutineScope = TestScope(dispatcher),
        ioDispatcher = dispatcher,
        timeSource = timeSource
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
            expected = State.Content(
                json = json,
                searchDirection = null,
                stats = Contract.Stats(
                    filePath = file.path,
                    fileName = file.name,
                    fileSize = "0.02KB",
                    fileReadTime = "0.000ms",
                    fileLines = "1"
                )
            ),
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
    fun `jsonTree parsing error returns Error state`() {
        val throwable = Throwable(message = "")
        underTest.showJsonParsingError(throwable)

        assertEquals(
            expected = State.Error(
                error = Contract.ErrorType.JsonParserError(message = throwable.localizedMessage)
            ),
            actual = underTest.state.value
        )
    }
}