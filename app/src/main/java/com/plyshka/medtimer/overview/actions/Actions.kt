package com.plyshka.medtimer.overview.actions


interface Actions {
    suspend fun buttonClicked(button: Button)

    val visibleButtons: List<Button>
}
