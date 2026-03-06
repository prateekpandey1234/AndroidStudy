package com.prateek.chatAppSystemDesign.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prateek.chatAppSystemDesign.other.NavigationEvent
import com.prateek.chatAppSystemDesign.other.NavigationEvents
import com.prateek.chatAppSystemDesign.presentation.viewModel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Modifier

@AndroidEntryPoint
class MainActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContent {
        }
    }

    @Composable
    fun ScreenHolder(modifier: Modifier,viewModel: ChatViewModel){
        val navigationController = rememberNavController()

        LaunchedEffect(Unit){
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvents.NavigateTo -> {
                        navigationController.navigate(event.route)
                    }
                    is NavigationEvents.NavigateBack -> {
                        Log.d("pop","backstack is empty")
                        if(navigationController.previousBackStackEntry != null) navigationController.popBackStack()
                        else{
                            finish()
                        }
                    }

                    else -> {}
                }
            }
        }

        NavHost(navController =  navigationController , startDestination = NavigationEvent.HOME){
            composable(NavigationEvent.HOME){

            }
            composable(NavigationEvent.CHANNEL){

            }
            composable(NavigationEvent.ATTACHMENT_IMAGE){

            }
            composable(NavigationEvent.ATTACHMENT_VIDEO){

            }
            composable(NavigationEvent.MESSAGE_VIEWS){

            }
        }




    }

}