package com.varunkumar.geminiapi.presentation.features.sense_feature

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnairePopup(modifier: Modifier = Modifier) {
    var index by remember {
        mutableIntStateOf(0)
    }

    var textFieldString by remember {
        mutableStateOf("")
    }

    BasicAlertDialog(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(10.dp),
        onDismissRequest = { /*TODO*/ }) {
        Column(modifier = modifier) {
            AnimatedContent(
                targetState = questions[index],
                label = "",
            ) { str ->
                Text(text = str)
            }

            OutlinedTextField(
                value = textFieldString,
                onValueChange = {
                    textFieldString = it
                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (questions.size > index) index++
                    }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuestionPrev() {
    QuestionnairePopup(modifier = Modifier.fillMaxWidth())
}

val questions = listOf(
    "What is your gender?",
    "How old are you?",
    "What is your weight?",
    "What is your height?",
    ""
)