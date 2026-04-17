package com.management.inventorypro.ui.theme.screens.register

import com.management.inventorypro.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.management.inventorypro.data.AuthViewModel


@Composable
fun RegisterScreen(navController: NavController){
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember {mutableStateOf("")}
    var confirmpassword by remember { mutableStateOf("")}

    var authViewModel: AuthViewModel = viewModel( )

    val context = LocalContext.current
    Column(
        modifier= Modifier.fillMaxSize(),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Image(
            painter = painterResource(id = R.drawable.ar12),
            contentDescription = "logo",
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(2.dp,Color.White,CircleShape)
                .shadow(4.dp,CircleShape))
        Text(text="Register Here",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
        OutlinedTextField(
            value = username,
            onValueChange = {username = it},
            label={Text(text="enter username")},
            placeholder = {Text(text="Please enter your username")},
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },

            )
        OutlinedTextField(
            value = phone,
            onValueChange = {phone = it},
            label = {Text(text = "enter phone number")},
            placeholder = {Text(text="please enter your phone number")},
            leadingIcon = {Icon(Icons.Default.Phone, contentDescription = null)}
        )

        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = {Text(text = "enter email")},
            placeholder = {Text(text="please enter your email")},
            leadingIcon = {Icon(Icons.Default.Email, contentDescription = null)}
        )
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = {Text(text = "enter your password")},
            visualTransformation = PasswordVisualTransformation(),
            placeholder = {Text(text="please enter your password")},
            leadingIcon = {Icon(Icons.Default.Lock, contentDescription = null)}
        )
        OutlinedTextField(
            value = confirmpassword,
            onValueChange = {confirmpassword = it},
            label = {Text(text = "confirm your password")},
            placeholder = {Text(text="please confirm your password")},
            visualTransformation = PasswordVisualTransformation(),

            leadingIcon = {Icon(Icons.Default.Check,contentDescription = null)}
        )
        Button(onClick = {authViewModel.signup(
            username=username,
            phone=phone,
            email=email,
            password=password,
            confirmpassword=confirmpassword,
            navController=navController,
            context=context)}) {Text(text = "register") }
        Row {
            Text(text = "Already Registered?",
                color=Color.Red)
            Spacer(modifier = Modifier.width(5.dp))
            Text(text = "log in here",
                color=Color.Blue,
                modifier = Modifier.clickable{
                    navController.navigate("login")

                })

        }
        Row {
            Text(text = "Bypass to Dashboard",
                color=Color.Red,
                modifier = Modifier.clickable{
                    navController.navigate("dashboard")

                })

        }

    }
}
@Preview(showBackground= true, showSystemUi = true)
@Composable
fun RegisterScreenPreview(){
    RegisterScreen(rememberNavController())
}