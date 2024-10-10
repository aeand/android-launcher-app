package com.example.my_launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    class ApplicationInformation {
        var label: String? = null
        var packageName: String? = null
        var icon: Drawable? = null
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) { //TODO figure out how to add settings
        super.onCreate(savedInstanceState)
        //TODO add settings. I wanna hide specific apps

        //TODO update date when date changes
        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

        //TODO set text color
        val textColor = Color.White

        // GET ALL APPS //TODO add refresh app list. donno when tho
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val packages: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)

        val apps = mutableListOf<ApplicationInformation>()
        for (app in packages) {
            val appInfo = ApplicationInformation()
            appInfo.label = app.loadLabel(packageManager).toString()
            appInfo.packageName = app.activityInfo.packageName
            appInfo.icon = app.loadIcon(packageManager)
            apps.add(appInfo)
        }
        apps.sortWith { a, b ->
            a.label?.compareTo(b.label!!)!!
        }

        // GET ALPHABET WITH APP NAMES
        val tempAlphabet = "1234567890qwertyuiopasdfghjklzxcvbnm".split("").dropLast(1).toMutableList()
        val alphabet = tempAlphabet.subList(1, tempAlphabet.size)
        alphabet.sortWith { a, b ->
            a.compareTo(b)
        }
        alphabet.add("å")
        alphabet.add("ä")
        alphabet.add("ö")

        val removeLetters = mutableListOf<String>()
        alphabet.forEach { letter ->
            var result = false
            apps.forEach { app ->
                if (!result) {
                    if (app.label != null && app.label!![0].uppercaseChar() == letter.toCharArray()[0].uppercaseChar()) {
                        result = true
                    }
                }
            }

            if (!result) {
                removeLetters.add(letter)
            }
        }

        removeLetters.forEach { letter ->
            alphabet.remove(letter)
        }

        setContent {
            //TODO make home button default to top of app list
            //TODO blur background
            //MylauncherTheme {}

            val isDarkMode = isSystemInDarkTheme()
            val context = LocalContext.current as ComponentActivity
            DisposableEffect(isDarkMode) {
                context.enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
                    navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
                )

                onDispose { }
            }

            VerticalPager( //TODO fix lag when going back to empty screen
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 48.dp),
                state = rememberPagerState(pageCount = {
                    2
                })
            ) {
                if (it == 1) {
                    Box(modifier = Modifier.fillMaxSize()) //TODO add widgets
                }
                else if (it == 0) {
                    //TODO put something else at the top. Like date, duolingo widget, brightness level
                    val scope = rememberCoroutineScope()
                    val lazyScroll = rememberLazyListState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp, end = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.4f)
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .align(Alignment.TopStart),
                                text = date,
                                color = textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight(600)
                            )

                            Icon(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(30.dp)
                                    .align(Alignment.BottomEnd)
                                    .clickable {
                                        scope.launch {
                                            lazyScroll.animateScrollToItem(0)
                                        }
                                    },
                                imageVector = Icons.Rounded.KeyboardArrowUp,
                                contentDescription = null,
                                tint = textColor
                            )
                        }

                        Row(
                            modifier = Modifier
                                .padding(start = 10.dp, end = 10.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(0.6f)
                                .align(Alignment.BottomEnd),
                            horizontalArrangement = Arrangement.End
                        ) {
                            //val appScroll = rememberScrollState()
                            val itemPositions: MutableList<Float> = mutableListOf()

                            LazyColumn(
                                modifier = Modifier
                                    .padding(end = 20.dp),
                                state = lazyScroll,
                                horizontalAlignment = Alignment.End
                            ) {
                                apps.forEach { app ->
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .onGloballyPositioned {
                                                    itemPositions.add(it.positionInRoot().y)
                                                }
                                                .padding(bottom = 20.dp)
                                                .clickable {
                                                    launchApp(app.packageName)
                                                }
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .padding(end = 10.dp)
                                                    .width(300.dp),
                                                text = "${app.label}",
                                                color = textColor,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight(weight = 700),
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1,
                                                textAlign = TextAlign.End
                                            )

                                            Image(
                                                modifier = Modifier
                                                    .size(50.dp),
                                                painter = rememberDrawablePainter(drawable = app.icon),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            }

                            Column( //TODO prettify the button press and animations
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                alphabet.forEach { letter ->
                                    Text(
                                        modifier = Modifier
                                            .clickable {
                                                scope.launch {
                                                    var i = 0

                                                    apps.forEachIndexed { index, app ->
                                                        if (i == 0 && app.label != null && app.label!![0].uppercaseChar() == letter.toCharArray()[0].uppercaseChar()) {
                                                            i = index
                                                        }
                                                    }

                                                    //appScroll.animateScrollTo(itemPositions[i].roundToInt() - 30)
                                                    lazyScroll.animateScrollToItem(i)
                                                }
                                            },
                                        text = letter,
                                        color = textColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight(600)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchApp(packageName: String?) {
        if (packageName == null)
            return

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        startActivity(launchIntent)
    }
}
