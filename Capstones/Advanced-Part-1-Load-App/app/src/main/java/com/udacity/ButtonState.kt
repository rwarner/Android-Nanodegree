package com.udacity


sealed class ButtonState {
    object Click : ButtonState()
    object Downloading : ButtonState()
    object Completed : ButtonState()
}