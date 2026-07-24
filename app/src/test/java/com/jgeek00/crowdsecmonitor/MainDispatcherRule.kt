package com.jgeek00.crowdsecmonitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit [TestWatcher] rule that replaces [Dispatchers.Main] with a [TestDispatcher]
 * so ViewModel [androidx.lifecycle.viewModelScope] coroutines run deterministically
 * under [kotlinx.coroutines.test.runTest].
 *
 * Usage: `@get:Rule val mainDispatcherRule = MainDispatcherRule()`
 */
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
