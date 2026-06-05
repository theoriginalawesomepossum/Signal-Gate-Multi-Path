// Inside ui/screens/CallLogScreen.kt
@Composable
fun CallLogScreen(
    modifier: Modifier = Modifier,
    // Inject your Koin ViewModel directly inside the Composable function wrapper
    viewModel: TelemetryViewModel = org.koin.androidx.compose.koinViewModel()
) {
    // Safely collect the reactive state flow inside lifecycle boundaries
    val callLogs by viewModel.liveCallTelemetry.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "TELEMETRY CALL LOG", color = TextPrimary, fontSize = 18.sp)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = callLogs, key = { it.id }) { log ->
                GlassmorphicCard {
                    // Visual rendering structure remains perfectly unchanged...
                }
            }
        }
    }
}