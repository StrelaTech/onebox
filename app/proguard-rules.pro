# Kotlin 2.x + kotlinx.coroutines fix
# Prevents: "Class kotlin.sequences.d extended by class sv0 is inaccessible"
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.sequences.** { *; }
-keep class kotlin.jvm.internal.** { *; }

# Keep MainDispatcher and related classes
-keep class kotlinx.coroutines.internal.MainDispatcherLoader { *; }
-keep class kotlinx.coroutines.Dispatchers { *; }
-keep class kotlinx.coroutines.CoroutineScopeKt { *; }
-keep class kotlinx.coroutines.CoroutineStart { *; }
-keep class kotlinx.coroutines.AbstractCoroutine { *; }
-keep class kotlinx.coroutines.BuildersKt { *; }
-keep class kotlinx.coroutines.BuildersKt__Builders_commonKt { *; }
-keep class kotlinx.coroutines.intrinsics.UndispatchedKt { *; }

# Keep Compose recomposer related
-keep class androidx.compose.ui.platform.WindowRecomposer** { *; }
-keep class androidx.compose.ui.platform.WindowRecomposerFactory { *; }
-keep class androidx.compose.ui.platform.WindowRecomposerPolicy { *; }
-keep class androidx.compose.ui.platform.AbstractComposeView { *; }

# R8 full mode compatibility
-allowaccessmodification