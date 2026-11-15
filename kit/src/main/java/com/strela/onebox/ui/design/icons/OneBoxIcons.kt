package com.strela.onebox.ui.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Иконки OneBox из набора [StrelaTech Icons](https://github.com/StrelaTech/icons).
 *
 * Сетка 24x24, обводка 2.5 px со скруглёнными концами и стыками. Исходные SVG
 * лежат в `app/src/main/res/icons/{outline,filled}`, файл сгенерирован
 * `scripts/import-icons.py` — правьте SVG и перегенерируйте, а не этот файл.
 *
 * Для нижней навигации пары: outline-варианты ([HomeOutline], [Globe],
 * [SettingsOutline]) рисуются для невыбранной вкладки, filled-варианты
 * ([Home], [GlobeFilled], [Settings]) — для выбранной.
 * Цвет задаётся при отрисовке через `Icon(tint = ...)`.
 */
object OneBoxIcons {

    /** StrelaTech: `outline/activity.svg`. */
    val Activity: ImageVector by lazy {
        icon("Activity", viewport = 24f) {
            path("M22 12L18 12L15 21L9 3L6 12L2 12", stroke = true)
        }
    }

    /** StrelaTech: `outline/alert.svg`. */
    val Alert: ImageVector by lazy {
        icon("Alert", viewport = 24f) {
            path("M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z", stroke = true)
            path("M12 9L12 13", stroke = true)
            path("M11.5 17a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `outline/arrow-down.svg`. */
    val ArrowDown: ImageVector by lazy {
        icon("ArrowDown", viewport = 24f) {
            path("M12 5L12 19", stroke = true, strokeWidth = 3f)
            path("M19 12L12 19L5 12", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/arrow-left.svg`. */
    val ArrowLeft: ImageVector by lazy {
        icon("ArrowLeft", viewport = 24f) {
            path("M19 12L5 12", stroke = true, strokeWidth = 3f)
            path("M12 19L5 12L12 5", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/arrow-right.svg`. */
    val ArrowRight: ImageVector by lazy {
        icon("ArrowRight", viewport = 24f) {
            path("M5 12L19 12", stroke = true, strokeWidth = 2f)
            path("M12 5L19 12L12 19", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `outline/arrow-up.svg`. */
    val ArrowUp: ImageVector by lazy {
        icon("ArrowUp", viewport = 24f) {
            path("M12 19L12 5", stroke = true, strokeWidth = 3f)
            path("M5 12L12 5L19 12", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/battery.svg`. */
    val Battery: ImageVector by lazy {
        icon("Battery", viewport = 24f) {
            path("M3 6h14a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2Z", stroke = true)
            path("M23 10L23 14", stroke = true)
            path("M5 10L5 14", stroke = true)
            path("M9 10L9 14", stroke = true)
            path("M13 10L13 14", stroke = true)
        }
    }

    /** StrelaTech: `outline/bell.svg`. */
    val Bell: ImageVector by lazy {
        icon("Bell", viewport = 24f) {
            path("M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9", stroke = true)
            path("M13.73 21a2 2 0 0 1-3.46 0", stroke = true)
        }
    }

    /** StrelaTech: `outline/bell-off.svg`. */
    val BellOff: ImageVector by lazy {
        icon("BellOff", viewport = 24f) {
            path("M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9", stroke = true)
            path("M13.73 21a2 2 0 0 1-3.46 0", stroke = true)
            path("M2 2L22 22", stroke = true)
        }
    }

    /** StrelaTech: `outline/bluetooth.svg`. */
    val Bluetooth: ImageVector by lazy {
        icon("Bluetooth", viewport = 24f) {
            path("M6.5 6.5L17.5 17.5L12 23V1L17.5 6.5L6.5 17.5", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `outline/bookmark.svg`. */
    val Bookmark: ImageVector by lazy {
        icon("Bookmark", viewport = 24f) {
            path("M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/c.svg`. */
    val C: ImageVector by lazy {
        icon("C", viewport = 24f) {
            path("M12 0L1.5 6V18L12 24L22.5 18V6L12 0ZM12 2.18L20.5 7.09V16.91L12 21.82L3.5 16.91V7.09L12 2.18Z", fill = true)
            path("M14.5 9.44C13.96 8.5 13.18 8 12.14 8C10.28 8 9 9.5 9 11.84C9 14.18 10.26 15.68 12.14 15.68C13.18 15.68 13.96 15.18 14.5 14.22L13.18 13.46C12.9 13.92 12.58 14.18 12.14 14.18C11.28 14.18 10.76 13.4 10.76 11.84C10.76 10.28 11.28 9.5 12.14 9.5C12.58 9.5 12.9 9.76 13.18 10.22L14.5 9.46V9.44Z", fill = true)
        }
    }

    /** StrelaTech: `outline/calendar.svg`. */
    val Calendar: ImageVector by lazy {
        icon("Calendar", viewport = 24f) {
            path("M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", stroke = true)
            path("M16 2L16 6", stroke = true)
            path("M8 2L8 6", stroke = true)
            path("M3 10L21 10", stroke = true)
        }
    }

    /** StrelaTech: `outline/camera.svg`. */
    val Camera: ImageVector by lazy {
        icon("Camera", viewport = 24f) {
            path("M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z", stroke = true)
            path("M8 13a4 4 0 1 0 8 0a4 4 0 1 0 -8 0Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/chat.svg`. */
    val Chat: ImageVector by lazy {
        icon("Chat", viewport = 24f) {
            path("M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/check.svg`. */
    val Check: ImageVector by lazy {
        icon("Check", viewport = 24f) {
            path("M20 6L9 17L4 12", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/clock.svg`. */
    val Clock: ImageVector by lazy {
        icon("Clock", viewport = 24f) {
            path("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0Z", stroke = true)
            path("M12 7L12 12L15 15", stroke = true)
        }
    }

    /** StrelaTech: `outline/close.svg`. */
    val Close: ImageVector by lazy {
        icon("Close", viewport = 24f) {
            path("M18 6L6 18", stroke = true)
            path("M6 6L18 18", stroke = true)
        }
    }

    /** StrelaTech: `outline/cloud.svg`. */
    val Cloud: ImageVector by lazy {
        icon("Cloud", viewport = 24f) {
            path("M7 18H17.5C19.9853 18 22 15.9853 22 13.5C22 11.0933 20.1126 9.12414 17.7376 9.00832C17.2367 5.76775 14.4358 3.5 11 3.5C7.5 3.5 4.6 6 4.2 9.3C2.35 9.85 1 11.55 1 13.5C1 15.9853 3.01472 18 5.5 18H7Z", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `outline/cloud-off.svg`. */
    val CloudOff: ImageVector by lazy {
        icon("CloudOff", viewport = 24f) {
            path("M7 18H17.5C19.9853 18 22 15.9853 22 13.5C22 11.0933 20.1126 9.12414 17.7376 9.00832C17.2367 5.76775 14.4358 3.5 11 3.5C7.5 3.5 4.6 6 4.2 9.3C2.35 9.85 1 11.55 1 13.5C1 15.9853 3.01472 18 5.5 18H7Z", stroke = true, strokeWidth = 2f)
            path("M3 3L21 21", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `outline/code.svg`. */
    val Code: ImageVector by lazy {
        icon("Code", viewport = 24f) {
            path("M16 18L22 12L16 6", stroke = true, strokeWidth = 3f)
            path("M8 6L2 12L8 18", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/cpp.svg`. */
    val Cpp: ImageVector by lazy {
        icon("Cpp", viewport = 24f) {
            path("M12 0L1.5 6v12L12 24l10.5-6V6L12 0zm0 2.18l8.5 4.91v9.82L12 21.82l-8.5-4.91V7.09L12 2.18z", fill = true)
            path("M11.1 9.6c-.54-.94-1.32-1.44-2.36-1.44-1.86 0-3.14 1.5-3.14 3.84s1.26 3.84 3.14 3.84c1.04 0 1.82-.5 2.36-1.46l-1.32-.76c-.28.46-.6.72-1.04.72-.86 0-1.38-.78-1.38-2.34s.52-2.34 1.38-2.34c.44 0 .76.26 1.04.72l1.32-.76z", fill = true)
            path("M14.5 12.9h-1.1v1.6h-1.1v-1.6h-1.1v-1.1h1.1v-1.6h1.1v1.6h1.1v1.1zm3.6 0h-1.1v1.6h-1.1v-1.6h-1.1v-1.1h1.1v-1.6h1.1v1.6h1.1v1.1z", fill = true)
        }
    }

    /** StrelaTech: `outline/credit-card.svg`. */
    val CreditCard: ImageVector by lazy {
        icon("CreditCard", viewport = 24f) {
            path("M3 4h18a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-18a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2Z", stroke = true)
            path("M1 10L23 10", stroke = true)
        }
    }

    /** StrelaTech: `outline/database.svg`. */
    val Database: ImageVector by lazy {
        icon("Database", viewport = 24f) {
            path("M3 5a9 3 0 1 0 18 0a9 3 0 1 0 -18 0Z", stroke = true)
            path("M21 12c0 1.66-4 3-9 3s-9-1.34-9-3", stroke = true)
            path("M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5", stroke = true)
        }
    }

    /** StrelaTech: `outline/download.svg`. */
    val Download: ImageVector by lazy {
        icon("Download", viewport = 24f) {
            path("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", stroke = true)
            path("M7 10L12 15L17 10", stroke = true)
            path("M12 15L12 3", stroke = true)
        }
    }

    /** StrelaTech: `outline/edit.svg`. */
    val Edit: ImageVector by lazy {
        icon("Edit", viewport = 24f) {
            path("M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7", stroke = true)
            path("M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z", stroke = true)
        }
    }

    /** StrelaTech: `outline/eye.svg`. */
    val Eye: ImageVector by lazy {
        icon("Eye", viewport = 24f) {
            path("M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z", stroke = true)
            path("M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/file.svg`. */
    val File: ImageVector by lazy {
        icon("File", viewport = 24f) {
            path("M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z", stroke = true)
            path("M14 2L14 8L20 8", stroke = true)
        }
    }

    /** StrelaTech: `outline/filter.svg`. */
    val Filter: ImageVector by lazy {
        icon("Filter", viewport = 24f) {
            path("M22 3L2 3L10 12.46L10 19L14 21L14 12.46L22 3Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/fingerprint.svg`. */
    val Fingerprint: ImageVector by lazy {
        icon("Fingerprint", viewport = 24f) {
            path("M1.5 11.9998C1.94799 8.86213 2.49181 7.26637 4.5 4.99976M7 2.99976C8.71302 1.31123 11.9382 1.48822 12 1.49989", stroke = true, strokeWidth = 1.5f)
            path("M15 2C16.5186 2.2355 17.1224 2.61429 18 3.5", stroke = true, strokeWidth = 1.5f)
            path("M19.5 5C21.2931 7.13732 22.0578 8.56245 22.5 12", stroke = true, strokeWidth = 1.5f)
            path("M6.5 6.5C7.80228 5.16267 8.57261 4.71716 10 4.5", stroke = true, strokeWidth = 1.5f)
            path("M13 4.5C19.6079 6.34614 21.2901 10.0494 18 20.5", stroke = true, strokeWidth = 1.5f)
            path("M16 8.49999C14.2127 7.02839 13.0716 6.67203 11 6.99999", stroke = true, strokeWidth = 1.5f)
            path("M17.5 13.5C17.8278 12.4667 17.4836 10.6267 17 10.5", stroke = true, strokeWidth = 1.5f)
            path("M17 16C16.4864 18.4794 15.9149 19.895 14 22.5", stroke = true, strokeWidth = 1.5f)
            path("M11 22.5C13.5885 18.9138 14.3625 16.51 14.5 11.5C13.9812 10.1908 13.3322 9.89191 12 9.5C10.5793 9.58472 10.1196 10.1589 9.5 11.5C9.0647 15.5784 8.47235 17.5813 6 20", stroke = true, strokeWidth = 1.5f)
            path("M12 12C11.8104 16.6401 10.8172 18.6404 8 21.5", stroke = true, strokeWidth = 1.5f)
            path("M2.5 16C3.33732 15.3141 3.72601 14.7075 4 12.5C4.02996 10.7168 4.09588 9.74708 5 8.5", stroke = true, strokeWidth = 1.5f)
            path("M4 18C5.31686 16.857 5.89432 15.9274 6.5 13.5C6.64997 11.2709 6.87483 10.1105 8 8.5", stroke = true, strokeWidth = 1.5f)
        }
    }

    /** StrelaTech: `outline/folder.svg`. */
    val Folder: ImageVector by lazy {
        icon("Folder", viewport = 24f) {
            path("M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/gauge.svg`. */
    val Gauge: ImageVector by lazy {
        icon("Gauge", viewport = 24f) {
            path("M4 14.89C4 12.7683 4.84285 10.7335 6.34315 9.23316C7.84344 7.73287 9.87827 6.89001 12 6.89001C14.1217 6.89001 16.1566 7.73287 17.6569 9.23316C19.1571 10.7335 20 12.7683 20 14.89", stroke = true)
            path("M12 15L15 12", stroke = true, strokeWidth = 1.5f)
            path("M12 16C12.5523 16 13 15.5523 13 15C13 14.4477 12.5523 14 12 14C11.4477 14 11 14.4477 11 15C11 15.5523 11.4477 16 12 16Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `outline/gift.svg`. */
    val Gift: ImageVector by lazy {
        icon("Gift", viewport = 24f) {
            path("M20 12L20 22L4 22L4 12", stroke = true)
            path("M2 7h20v5h-20Z", stroke = true)
            path("M12 22L12 7", stroke = true)
            path("M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z", stroke = true)
            path("M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z", stroke = true)
        }
    }

    /** StrelaTech: `outline/github.svg`. */
    val Github: ImageVector by lazy {
        icon("Github", viewport = 24f) {
            path("M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12", stroke = true, strokeWidth = 1.5f)
        }
    }

    /** StrelaTech: `outline/globe.svg`. */
    val Globe: ImageVector by lazy {
        icon("Globe", viewport = 24f) {
            path("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0Z", stroke = true, strokeWidth = 2f)
            path("M3 12L21 12", stroke = true, strokeWidth = 2f)
            path("M12 3a15 15 0 0 1 0 18 15 15 0 0 1 0-18z", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `outline/headphones.svg`. */
    val Headphones: ImageVector by lazy {
        icon("Headphones", viewport = 24f) {
            path("M3 18v-6a9 9 0 0 1 18 0v6", stroke = true)
            path("M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z", stroke = true)
        }
    }

    /** StrelaTech: `outline/heart.svg`. */
    val Heart: ImageVector by lazy {
        icon("Heart", viewport = 24f) {
            path("M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z", stroke = true)
        }
    }

    /** StrelaTech: `outline/home.svg`. */
    val HomeOutline: ImageVector by lazy {
        icon("HomeOutline", viewport = 24f) {
            path("M3 10.5L12 3l9 7.5", stroke = true)
            path("M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5", stroke = true)
        }
    }

    /** StrelaTech: `outline/image.svg`. */
    val Image: ImageVector by lazy {
        icon("Image", viewport = 24f) {
            path("M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", stroke = true)
            path("M7 8.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0Z", stroke = true)
            path("M21 15l-5-5L5 21", stroke = true)
        }
    }

    /** StrelaTech: `outline/info.svg`. */
    val Info: ImageVector by lazy {
        icon("Info", viewport = 24f) {
            path("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0Z", stroke = true, strokeWidth = 2f)
            path("M12 11L12 16", stroke = true, strokeWidth = 2f)
            path("M11.5 8a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0Z", fill = true, stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `outline/key.svg`. */
    val Key: ImageVector by lazy {
        icon("Key", viewport = 24f) {
            path("M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4", stroke = true)
        }
    }

    /** StrelaTech: `outline/link.svg`. */
    val Link: ImageVector by lazy {
        icon("Link", viewport = 24f) {
            path("M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71", stroke = true)
            path("M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71", stroke = true)
        }
    }

    /** StrelaTech: `outline/lock.svg`. */
    val Lock: ImageVector by lazy {
        icon("Lock", viewport = 24f) {
            path("M6 11h12a2 2 0 0 1 2 2v6a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-6a2 2 0 0 1 2 -2Z", stroke = true, strokeWidth = 3f)
            path("M8 11V7a4 4 0 0 1 8 0v4", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/mail.svg`. */
    val Mail: ImageVector by lazy {
        icon("Mail", viewport = 24f) {
            path("M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2Z", stroke = true)
            path("M2 7l10 7 10-7", stroke = true)
        }
    }

    /** StrelaTech: `outline/map-pin.svg`. */
    val MapPin: ImageVector by lazy {
        icon("MapPin", viewport = 24f) {
            path("M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z", stroke = true, strokeWidth = 2.4f)
            path("M9 10a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true, strokeWidth = 2.4f)
        }
    }

    /** StrelaTech: `outline/menu.svg`. */
    val Menu: ImageVector by lazy {
        icon("Menu", viewport = 24f) {
            path("M4 6L20 6", stroke = true, strokeWidth = 3f)
            path("M4 12L20 12", stroke = true, strokeWidth = 3f)
            path("M4 18L20 18", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/mic.svg`. */
    val Mic: ImageVector by lazy {
        icon("Mic", viewport = 24f) {
            path("M12 1h0a3 3 0 0 1 3 3v5a3 3 0 0 1 -3 3h0a3 3 0 0 1 -3 -3v-5a3 3 0 0 1 3 -3Z", stroke = true)
            path("M5 10a7 7 0 0 0 14 0", stroke = true)
            path("M12 19L12 22", stroke = true)
        }
    }

    /** StrelaTech: `outline/minus.svg`. */
    val Minus: ImageVector by lazy {
        icon("Minus", viewport = 24f) {
            path("M5 12L19 12", stroke = true)
        }
    }

    /** StrelaTech: `outline/monitor.svg`. */
    val Monitor: ImageVector by lazy {
        icon("Monitor", viewport = 24f) {
            path("M4 3h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z", stroke = true)
            path("M8 21L16 21", stroke = true)
            path("M12 17L12 21", stroke = true)
        }
    }

    /** StrelaTech: `outline/moon.svg`. */
    val Moon: ImageVector by lazy {
        icon("Moon", viewport = 24f) {
            path("M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z", stroke = true)
        }
    }

    /** StrelaTech: `outline/music.svg`. */
    val Music: ImageVector by lazy {
        icon("Music", viewport = 24f) {
            path("M9 18V5l12-2v13", stroke = true)
            path("M3 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
            path("M15 16a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/onebox-logo.svg`. */
    val OneBoxLogo: ImageVector by lazy {
        icon("OneBoxLogo", viewport = 24f) {
            path("M5.25733 9.57178C5.2562 9.16983 5.6882 8.91666 6.03053 9.1186L11.5969 12.4026C11.7528 12.4945 11.8486 12.663 11.8491 12.846L11.8673 19.382C11.8684 19.7839 11.4364 20.0371 11.0941 19.8352L5.5277 16.5512C5.37189 16.4593 5.27603 16.2907 5.27551 16.1077L5.25733 9.57178Z", fill = true)
            path("M6.2039 8.97694C5.86204 8.77809 5.86204 8.28431 6.2039 8.08547L11.7877 4.8382C11.9479 4.74501 12.1458 4.74501 12.3061 4.8382L17.8898 8.08547C18.2317 8.28431 18.2317 8.77809 17.8898 8.97694L12.3061 12.2242C12.1458 12.3174 11.9479 12.3174 11.7877 12.2242L6.2039 8.97694Z", fill = true)
            path("M18.6959 9.57178C18.697 9.16983 18.265 8.91666 17.9227 9.1186L12.3563 12.4026C12.2005 12.4945 12.1046 12.663 12.1041 12.846L12.0859 19.382C12.0848 19.7839 12.5168 20.0371 12.8591 19.8352L18.4255 16.5512C18.5814 16.4593 18.6772 16.2907 18.6777 16.1077L18.6959 9.57178Z", fill = true)
        }
    }

    /** StrelaTech: `outline/pause.svg`. */
    val Pause: ImageVector by lazy {
        icon("Pause", viewport = 24f) {
            path("M7 4h2a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1v-14a1 1 0 0 1 1 -1Z", stroke = true)
            path("M15 4h2a1 1 0 0 1 1 1v14a1 1 0 0 1 -1 1h-2a1 1 0 0 1 -1 -1v-14a1 1 0 0 1 1 -1Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/phone.svg`. */
    val Phone: ImageVector by lazy {
        icon("Phone", viewport = 24f) {
            path("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.13.96.36 1.9.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.91.34 1.85.57 2.81.7A2 2 0 0 1 22 16.92z", stroke = true)
        }
    }

    /** StrelaTech: `outline/play.svg`. */
    val Play: ImageVector by lazy {
        icon("Play", viewport = 24f) {
            path("M6 3L20 12L6 21L6 3Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/plus.svg`. */
    val Plus: ImageVector by lazy {
        icon("Plus", viewport = 24f) {
            path("M12 5L12 19", stroke = true)
            path("M5 12L19 12", stroke = true)
        }
    }

    /** StrelaTech: `outline/power.svg`. */
    val Power: ImageVector by lazy {
        icon("Power", viewport = 24f) {
            path("M18.36 6.64a9 9 0 1 1-12.73 0", stroke = true)
            path("M12 2L12 12", stroke = true)
        }
    }

    /** StrelaTech: `outline/python.svg`. */
    val Python: ImageVector by lazy {
        icon("Python", viewport = 24f) {
            path("M11.914 0C5.82 0 6.2 2.656 6.2 2.656l.007 2.752h5.814v.826H3.9S0 5.789 0 11.969c0 6.18 3.403 5.96 3.403 5.96h2.03v-2.867s-.109-3.403 3.35-3.403h5.768s3.241.052 3.241-3.135V3.189S18.277 0 11.914 0z", stroke = true, strokeWidth = 1.5f)
            path("M7.65 2.94a1.058 1.058 0 1 0 2.116 0a1.058 1.058 0 1 0 -2.116 0Z", stroke = true, strokeWidth = 1.5f)
            path("M12.086 24c6.094 0 5.714-2.656 5.714-2.656l-.007-2.752h-5.814v-.826h8.121S24 18.211 24 12.031c0-6.18-3.403-5.96-3.403-5.96h-2.03v2.867s.109 3.403-3.35 3.403H9.449s-3.241-.052-3.241 3.135v5.335S5.723 24 12.086 24z", stroke = true, strokeWidth = 1.5f)
            path("M14.236 22.06a1.058 1.058 0 1 0 2.116 0a1.058 1.058 0 1 0 -2.116 0Z", stroke = true, strokeWidth = 1.5f)
        }
    }

    /** StrelaTech: `outline/refresh.svg`. */
    val Refresh: ImageVector by lazy {
        icon("Refresh", viewport = 24f) {
            path("M1 4L1 10L7 10", stroke = true, strokeWidth = 3f)
            path("M3.51 15a9 9 0 1 0 2.13-9.36L1 10", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `outline/resize.svg`. */
    val Resize: ImageVector by lazy {
        icon("Resize", viewport = 24f) {
            path("M15 3L21 3L21 9", stroke = true)
            path("M9 21L3 21L3 15", stroke = true)
            path("M21 3L14 10", stroke = true)
            path("M3 21L10 14", stroke = true)
        }
    }

    /** StrelaTech: `outline/scale.svg`. */
    val Scale: ImageVector by lazy {
        icon("Scale", viewport = 24f) {
            path("M12 3V21", stroke = true)
            path("M5 21H19", stroke = true)
            path("M5 7H19Z", fill = true)
            path("M5 7H19", stroke = true)
            path("M8 11C8 11.7956 7.68393 12.5587 7.12132 13.1213C6.55871 13.6839 5.79565 14 5 14C4.20435 14 3.44129 13.6839 2.87868 13.1213C2.31607 12.5587 2 11.7956 2 11", stroke = true)
            path("M22 11C22 11.7956 21.6839 12.5587 21.1213 13.1213C20.5587 13.6839 19.7956 14 19 14C18.2044 14 17.4413 13.6839 16.8787 13.1213C16.3161 12.5587 16 11.7956 16 11", stroke = true)
        }
    }

    /** StrelaTech: `outline/search.svg`. */
    val Search: ImageVector by lazy {
        icon("Search", viewport = 24f) {
            path("M4 11a7 7 0 1 0 14 0a7 7 0 1 0 -14 0Z", stroke = true)
            path("M16.5 16.5L21 21", stroke = true)
        }
    }

    /** StrelaTech: `outline/send.svg`. */
    val Send: ImageVector by lazy {
        icon("Send", viewport = 24f) {
            path("M22 2L11 13", stroke = true)
            path("M22 2L15 22L11 13L2 9L22 2Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/server.svg`. */
    val Server: ImageVector by lazy {
        icon("Server", viewport = 24f) {
            path("M4 2h16a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-4a2 2 0 0 1 2 -2Z", stroke = true)
            path("M4 14h16a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-4a2 2 0 0 1 2 -2Z", stroke = true)
            path("M6 6L6.01 6", stroke = true)
            path("M6 18L6.01 18", stroke = true)
        }
    }

    /** StrelaTech: `outline/settings.svg`. */
    val SettingsOutline: ImageVector by lazy {
        icon("SettingsOutline", viewport = 24f) {
            path("M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
            path("M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z", stroke = true)
        }
    }

    /** StrelaTech: `outline/share.svg`. */
    val Share: ImageVector by lazy {
        icon("Share", viewport = 24f) {
            path("M15 5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
            path("M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
            path("M15 19a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z", stroke = true)
            path("M8.59 10.49L15.42 6.51", stroke = true)
            path("M8.59 13.51L15.42 17.49", stroke = true)
        }
    }

    /** StrelaTech: `outline/shield.svg`. */
    val Shield: ImageVector by lazy {
        icon("Shield", viewport = 24f) {
            path("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z", stroke = true)
        }
    }

    /** StrelaTech: `outline/shopping-cart.svg`. */
    val ShoppingCart: ImageVector by lazy {
        icon("ShoppingCart", viewport = 24f) {
            path("M8 21a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", stroke = true)
            path("M19 21a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", stroke = true)
            path("M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6", stroke = true)
        }
    }

    /** StrelaTech: `outline/smartphone.svg`. */
    val Smartphone: ImageVector by lazy {
        icon("Smartphone", viewport = 24f) {
            path("M7 1h10a2 2 0 0 1 2 2v18a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-18a2 2 0 0 1 2 -2Z", stroke = true)
            path("M12 18L12 18.01", stroke = true)
        }
    }

    /** StrelaTech: `outline/star.svg`. */
    val Star: ImageVector by lazy {
        icon("Star", viewport = 24f) {
            path("M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/strela-logo.svg`. */
    val StrelaLogo: ImageVector by lazy {
        icon("StrelaLogo", viewport = 24f) {
            path("M0.7 12a11.3 11.3 0 1 0 22.6 0a11.3 11.3 0 1 0 -22.6 0Z", stroke = true, strokeWidth = 1.4f)
            path("M8.7 12a3.3 3.3 0 1 0 6.6 0a3.3 3.3 0 1 0 -6.6 0Z", stroke = true, strokeWidth = 1.4f)
            path("M11.2 12a0.8 0.8 0 1 0 1.6 0a0.8 0.8 0 1 0 -1.6 0Z", fill = true, stroke = true, strokeWidth = 1.4f)
            path("M14.75 4.48486C17.8137 5.60623 20 8.54796 20 12C20 16.4183 16.4183 20 12 20C7.58172 20 4 16.4183 4 12C4 8.54796 6.18636 5.60623 9.25 4.48486V5.99854C6.97788 7.04142 5.3999 9.33642 5.3999 12C5.3999 15.6451 8.3549 18.6001 12 18.6001C15.6451 18.6001 18.6001 15.6451 18.6001 12C18.6001 9.33642 17.0221 7.04142 14.75 5.99854V4.48486Z", fill = true)
            path("M13.9773 3.78977L13.0398 3.89204C13.0132 3.79735 12.9668 3.70834 12.9006 3.625C12.8361 3.54167 12.749 3.47443 12.6392 3.4233C12.5293 3.37216 12.3949 3.34659 12.2358 3.34659C12.0218 3.34659 11.8418 3.39299 11.696 3.48579C11.5521 3.5786 11.4811 3.69886 11.483 3.84659C11.4811 3.97348 11.5274 4.0767 11.6222 4.15625C11.7188 4.23579 11.8778 4.30114 12.0994 4.35227L12.8438 4.51136C13.2567 4.60038 13.5634 4.74148 13.7642 4.93466C13.9668 5.12784 14.0691 5.38068 14.071 5.69318C14.0691 5.9678 13.9886 6.21023 13.8295 6.42045C13.6723 6.62879 13.4536 6.79167 13.1733 6.90909C12.893 7.02651 12.571 7.08523 12.2074 7.08523C11.6733 7.08523 11.2434 6.97348 10.9176 6.75C10.5919 6.52462 10.3977 6.21117 10.3352 5.80966L11.3381 5.71307C11.3835 5.91004 11.4801 6.05871 11.6278 6.15909C11.7756 6.25947 11.9678 6.30966 12.2045 6.30966C12.4489 6.30966 12.6449 6.25947 12.7926 6.15909C12.9422 6.05871 13.017 5.93466 13.017 5.78693C13.017 5.66193 12.9688 5.55871 12.8722 5.47727C12.7775 5.39583 12.6297 5.33333 12.429 5.28977L11.6847 5.13352C11.2661 5.0464 10.9564 4.89962 10.7557 4.69318C10.5549 4.48485 10.4555 4.22159 10.4574 3.90341C10.4555 3.63447 10.5284 3.40152 10.6761 3.20455C10.8258 3.00568 11.0331 2.85227 11.2983 2.74432C11.5653 2.63447 11.8731 2.57955 12.2216 2.57955C12.7329 2.57955 13.1354 2.68845 13.429 2.90625C13.7244 3.12405 13.9072 3.41856 13.9773 3.78977Z", fill = true)
        }
    }

    /** StrelaTech: `outline/sun.svg`. */
    val Sun: ImageVector by lazy {
        icon("Sun", viewport = 24f) {
            path("M7 12a5 5 0 1 0 10 0a5 5 0 1 0 -10 0Z", stroke = true)
            path("M12 1L12 3", stroke = true)
            path("M12 21L12 23", stroke = true)
            path("M4.22 4.22L5.64 5.64", stroke = true)
            path("M18.36 18.36L19.78 19.78", stroke = true)
            path("M1 12L3 12", stroke = true)
            path("M21 12L23 12", stroke = true)
            path("M4.22 19.78L5.64 18.36", stroke = true)
            path("M18.36 5.64L19.78 4.22", stroke = true)
        }
    }

    /** StrelaTech: `outline/tag.svg`. */
    val Tag: ImageVector by lazy {
        icon("Tag", viewport = 24f) {
            path("M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z", stroke = true)
            path("M6 7a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `outline/terminal.svg`. */
    val Terminal: ImageVector by lazy {
        icon("Terminal", viewport = 24f) {
            path("M4 17L10 11L4 5", stroke = true)
            path("M12 19L20 19", stroke = true)
        }
    }

    /** StrelaTech: `outline/timer.svg`. */
    val Timer: ImageVector by lazy {
        icon("Timer", viewport = 24f) {
            path("M4 13a8 8 0 1 0 16 0a8 8 0 1 0 -16 0Z", stroke = true)
            path("M12 9L12 13", stroke = true)
            path("M10 2L14 2", stroke = true)
        }
    }

    /** StrelaTech: `outline/trash.svg`. */
    val Trash: ImageVector by lazy {
        icon("Trash", viewport = 24f) {
            path("M3 6L5 6L21 6", stroke = true)
            path("M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6", stroke = true)
            path("M10 11v6", stroke = true)
            path("M14 11v6", stroke = true)
            path("M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2", stroke = true)
        }
    }

    /** StrelaTech: `outline/upload.svg`. */
    val Upload: ImageVector by lazy {
        icon("Upload", viewport = 24f) {
            path("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", stroke = true)
            path("M17 8L12 3L7 8", stroke = true)
            path("M12 3L12 15", stroke = true)
        }
    }

    /** StrelaTech: `outline/user.svg`. */
    val User: ImageVector by lazy {
        icon("User", viewport = 24f) {
            path("M8 8a4 4 0 1 0 8 0a4 4 0 1 0 -8 0Z", stroke = true)
            path("M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1", stroke = true)
        }
    }

    /** StrelaTech: `outline/video.svg`. */
    val Video: ImageVector by lazy {
        icon("Video", viewport = 24f) {
            path("M3 5h11a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-11a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z", stroke = true)
            path("M16 10L23 7L23 17L16 14Z", stroke = true)
        }
    }

    /** StrelaTech: `outline/volume.svg`. */
    val Volume: ImageVector by lazy {
        icon("Volume", viewport = 24f) {
            path("M11 5L6 9L2 9L2 15L6 15L11 19L11 5Z", stroke = true)
            path("M15.54 8.46a5 5 0 0 1 0 7.07", stroke = true)
            path("M19.07 4.93a10 10 0 0 1 0 14.14", stroke = true)
        }
    }

    /** StrelaTech: `outline/wifi.svg`. */
    val Wifi: ImageVector by lazy {
        icon("Wifi", viewport = 24f) {
            path("M5 12.55a11 11 0 0 1 14.08 0", stroke = true)
            path("M1.42 9a16 16 0 0 1 21.16 0", stroke = true)
            path("M8.53 16.11a6 6 0 0 1 6.95 0", stroke = true)
            path("M11 20a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `outline/zap.svg`. */
    val Zap: ImageVector by lazy {
        icon("Zap", viewport = 24f) {
            path("M13 2L3 14L12 14L11 22L21 10L12 10L13 2Z", stroke = true)
        }
    }

    /** StrelaTech: `filled/activity.svg`. */
    val ActivityFilled: ImageVector by lazy {
        icon("ActivityFilled", viewport = 24f) {
            path("M22 12L18 12L15 21L9 3L6 12L2 12", stroke = true)
        }
    }

    /** StrelaTech: `filled/alert.svg`. */
    val AlertFilled: ImageVector by lazy {
        icon("AlertFilled", viewport = 24f) {
            path("M12 1.64746C12.5583 1.6475 13.1072 1.7916 13.5937 2.06543C14.0804 2.33944 14.4886 2.73438 14.7783 3.21191L14.7822 3.21777L23.2519 17.3574C23.2554 17.3631 23.2594 17.3692 23.2627 17.375C23.5464 17.8664 23.6966 18.4238 23.6982 18.9912C23.6998 19.5585 23.5523 20.1165 23.2715 20.6094C22.9905 21.1023 22.5847 21.514 22.0957 21.8018C21.6068 22.0893 21.0506 22.2438 20.4834 22.25H3.51561C2.94838 22.2437 2.39221 22.0894 1.90331 21.8018C1.41428 21.514 1.0095 21.1023 0.728504 20.6094C0.447573 20.1165 0.300194 19.5586 0.301746 18.9912C0.303337 18.4238 0.453614 17.8664 0.737293 17.375C0.740563 17.3693 0.74371 17.363 0.747059 17.3574L9.21776 3.21777L9.22069 3.21191L9.33495 3.03711C9.61371 2.63781 9.9794 2.30526 10.4053 2.06543C10.8919 1.79144 11.4415 1.6475 12 1.64746ZM12 15.5C11.1716 15.5 10.5 16.1716 10.5 17C10.5 17.8284 11.1716 18.5 12 18.5C12.8284 18.5 13.5 17.8284 13.5 17C13.5 16.1716 12.8284 15.5 12 15.5ZM12 7.75C11.3096 7.75001 10.75 8.30965 10.75 9V13C10.75 13.6904 11.3096 14.25 12 14.25C12.6903 14.25 13.25 13.6904 13.25 13V9C13.25 8.30964 12.6903 7.75 12 7.75Z", fill = true)
        }
    }

    /** StrelaTech: `filled/arrow-down.svg`. */
    val ArrowDownFilled: ImageVector by lazy {
        icon("ArrowDownFilled", viewport = 24f) {
            path("M12 5L12 19", stroke = true, strokeWidth = 3f)
            path("M19 12L12 19L5 12", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/arrow-left.svg`. */
    val ArrowLeftFilled: ImageVector by lazy {
        icon("ArrowLeftFilled", viewport = 24f) {
            path("M19 12L5 12", stroke = true, strokeWidth = 3f)
            path("M12 19L5 12L12 5", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/arrow-right.svg`. */
    val ArrowRightFilled: ImageVector by lazy {
        icon("ArrowRightFilled", viewport = 24f) {
            path("M5 12L19 12", stroke = true, strokeWidth = 2f)
            path("M12 5L19 12L12 19", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `filled/arrow-up.svg`. */
    val ArrowUpFilled: ImageVector by lazy {
        icon("ArrowUpFilled", viewport = 24f) {
            path("M12 19L12 5", stroke = true, strokeWidth = 3f)
            path("M5 12L12 5L19 12", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/battery.svg`. */
    val BatteryFilled: ImageVector by lazy {
        icon("BatteryFilled", viewport = 24f) {
            path("M17 4.75C18.7949 4.75 20.25 6.20507 20.25 8V16C20.25 17.7949 18.7949 19.25 17 19.25H3C1.20507 19.25 -0.25 17.7949 -0.25 16V8C-0.25 6.20507 1.20507 4.75 3 4.75H17ZM5 8.75C4.30964 8.75 3.75 9.30964 3.75 10V14C3.75 14.6904 4.30964 15.25 5 15.25C5.69036 15.25 6.25 14.6904 6.25 14V10C6.25 9.30964 5.69036 8.75 5 8.75ZM9 8.75C8.30964 8.75 7.75 9.30964 7.75 10V14C7.75 14.6904 8.30964 15.25 9 15.25C9.69036 15.25 10.25 14.6904 10.25 14V10C10.25 9.30964 9.69036 8.75 9 8.75ZM13 8.75C12.3096 8.75 11.75 9.30964 11.75 10V14C11.75 14.6904 12.3096 15.25 13 15.25C13.6904 15.25 14.25 14.6904 14.25 14V10C14.25 9.30964 13.6904 8.75 13 8.75Z", fill = true)
            path("M23 10V14", stroke = true)
        }
    }

    /** StrelaTech: `filled/bell.svg`. */
    val BellFilled: ImageVector by lazy {
        icon("BellFilled", viewport = 24f) {
            path("M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z", fill = true, stroke = true)
            path("M13.73 21C13.5542 21.3031 13.3018 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21", stroke = true)
        }
    }

    /** StrelaTech: `filled/bell-off.svg`. */
    val BellOffFilled: ImageVector by lazy {
        icon("BellOffFilled", viewport = 24f) {
            path("M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z", fill = true, stroke = true)
            path("M13.73 21C13.5542 21.3031 13.3018 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21", stroke = true)
            path("M2 2L22 22", stroke = true)
        }
    }

    /** StrelaTech: `filled/bluetooth.svg`. */
    val BluetoothFilled: ImageVector by lazy {
        icon("BluetoothFilled", viewport = 24f) {
            path("M6.5 6.5L17.5 17.5L12 23V1L17.5 6.5L6.5 17.5", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `filled/bookmark.svg`. */
    val BookmarkFilled: ImageVector by lazy {
        icon("BookmarkFilled", viewport = 24f) {
            path("M19 21L12 16L5 21V5C5 4.46957 5.21071 3.96086 5.58579 3.58579C5.96086 3.21071 6.46957 3 7 3H17C17.5304 3 18.0391 3.21071 18.4142 3.58579C18.7893 3.96086 19 4.46957 19 5V21Z", fill = true, stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/c.svg`. */
    val CFilled: ImageVector by lazy {
        icon("CFilled", viewport = 24f) {
            path("M20.6602 7V17L12 22L3.33984 17V7L12 2L20.6602 7ZM12.1396 8C10.2799 8.00019 9.00006 9.50008 9 11.8398C9 14.1797 10.2598 15.6795 12.1396 15.6797C13.1796 15.6797 13.96 15.1797 14.5 14.2197L13.1797 13.46C12.8997 13.9199 12.5796 14.1797 12.1396 14.1797C11.2799 14.1795 10.7598 13.3996 10.7598 11.8398C10.7598 10.2802 11.2799 9.50021 12.1396 9.5C12.5795 9.5 12.8997 9.75994 13.1797 10.2197L14.5 9.45996V9.44043C13.96 8.50043 13.1796 8 12.1396 8Z", fill = true)
            path("M12 0L1.5 6V18L12 24L22.5 18V6L12 0ZM12 2.18L20.5 7.09V16.91L12 21.82L3.5 16.91V7.09L12 2.18Z", fill = true)
        }
    }

    /** StrelaTech: `filled/calendar.svg`. */
    val CalendarFilled: ImageVector by lazy {
        icon("CalendarFilled", viewport = 24f) {
            path("M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", fill = true, stroke = true)
            path("M8 2L8 6", stroke = true)
            path("M16 2L16 6", stroke = true)
            path("M3 10L21 10", stroke = true)
        }
    }

    /** StrelaTech: `filled/camera.svg`. */
    val CameraFilled: ImageVector by lazy {
        icon("CameraFilled", viewport = 24f) {
            path("M15 1.75C15.4179 1.75 15.8082 1.95889 16.04 2.30664L17.6689 4.75H21C21.862 4.75 22.6884 5.09266 23.2979 5.70215C23.9073 6.31164 24.25 7.13805 24.25 8V19C24.25 19.862 23.9073 20.6884 23.2979 21.2979C22.6884 21.9073 21.862 22.25 21 22.25H3C2.13805 22.25 1.31164 21.9073 0.702148 21.2979C0.0926556 20.6884 -0.25 19.862 -0.25 19V8C-0.25 7.13805 0.0926552 6.31164 0.702148 5.70215C1.31164 5.09266 2.13805 4.75 3 4.75H6.33105L7.95996 2.30664L8.05371 2.18262C8.28944 1.90966 8.63429 1.75 9 1.75H15ZM12 9C9.79086 9 8 10.7909 8 13C8 15.2091 9.79086 17 12 17C14.2091 17 16 15.2091 16 13C16 10.7909 14.2091 9 12 9Z", fill = true)
        }
    }

    /** StrelaTech: `filled/chat.svg`. */
    val ChatFilled: ImageVector by lazy {
        icon("ChatFilled", viewport = 24f) {
            path("M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z", fill = true, stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/check.svg`. */
    val CheckFilled: ImageVector by lazy {
        icon("CheckFilled", viewport = 24f) {
            path("M20 6L9 17L4 12", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/clock.svg`. */
    val ClockFilled: ImageVector by lazy {
        icon("ClockFilled", viewport = 24f) {
            path("M12 1.75C17.6609 1.75 22.25 6.33908 22.25 12C22.25 17.6609 17.6609 22.25 12 22.25C6.33908 22.25 1.75 17.6609 1.75 12C1.75 6.33908 6.33908 1.75 12 1.75ZM12 5.75C11.3096 5.75 10.75 6.30964 10.75 7V12C10.75 12.3315 10.8818 12.6494 11.1162 12.8838L14.1162 15.8838C14.6044 16.3719 15.3956 16.3719 15.8838 15.8838C16.3719 15.3956 16.3719 14.6044 15.8838 14.1162L13.25 11.4824V7C13.25 6.30964 12.6904 5.75 12 5.75Z", fill = true)
        }
    }

    /** StrelaTech: `filled/close.svg`. */
    val CloseFilled: ImageVector by lazy {
        icon("CloseFilled", viewport = 24f) {
            path("M18 6L6 18", stroke = true)
            path("M6 6L18 18", stroke = true)
        }
    }

    /** StrelaTech: `filled/cloud.svg`. */
    val CloudFilled: ImageVector by lazy {
        icon("CloudFilled", viewport = 24f) {
            path("M7 18H17.5C19.9853 18 22 15.9853 22 13.5C22 11.0933 20.1126 9.12414 17.7376 9.00832C17.2367 5.76775 14.4358 3.5 11 3.5C7.5 3.5 4.6 6 4.2 9.3C2.35 9.85 1 11.55 1 13.5C1 15.9853 3.01472 18 5.5 18H7Z", fill = true, stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `filled/cloud-off.svg`. */
    val CloudOffFilled: ImageVector by lazy {
        icon("CloudOffFilled", viewport = 24f) {
            path("M7 18H17.5C19.9853 18 22 15.9853 22 13.5C22 11.0933 20.1126 9.12414 17.7376 9.00832C17.2367 5.76775 14.4358 3.5 11 3.5C7.5 3.5 4.6 6 4.2 9.3C2.35 9.85 1 11.55 1 13.5C1 15.9853 3.01472 18 5.5 18H7Z", fill = true, stroke = true, strokeWidth = 2f)
            path("M3 3L21 21", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `filled/code.svg`. */
    val CodeFilled: ImageVector by lazy {
        icon("CodeFilled", viewport = 24f) {
            path("M16 18L22 12L16 6", stroke = true, strokeWidth = 3f)
            path("M8 6L2 12L8 18", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/cpp.svg`. */
    val CppFilled: ImageVector by lazy {
        icon("CppFilled", viewport = 24f) {
            path("M20.6602 7V17L12 22L3.33984 17V7L12 2L20.6602 7ZM8.74023 8.16016C6.88023 8.16016 5.59961 9.66 5.59961 12C5.59961 14.34 6.86023 15.8398 8.74023 15.8398C9.78009 15.8398 10.5597 15.3398 11.0996 14.3799L9.78027 13.6201C9.50032 14.08 9.18011 14.3398 8.74023 14.3398C7.88023 14.3398 7.36035 13.56 7.36035 12C7.36035 10.44 7.88023 9.66016 8.74023 9.66016C9.18011 9.66025 9.50032 9.91996 9.78027 10.3799L11.0996 9.62012V9.59961C10.5597 8.65998 9.77995 8.16023 8.74023 8.16016ZM12.2998 10.2002V11.7998H11.2002V12.9004H12.2998V14.5H13.4004V12.9004H14.5V11.7998H13.4004V10.2002H12.2998ZM15.9004 10.2002V11.7998H14.7998V12.9004H15.9004V14.5H17V12.9004H18.0996V11.7998H17V10.2002H15.9004Z", fill = true)
            path("M12 0L1.5 6V18L12 24L22.5 18V6L12 0ZM12 2.18L20.5 7.09V16.91L12 21.82L3.5 16.91V7.09L12 2.18Z", fill = true)
        }
    }

    /** StrelaTech: `filled/credit-card.svg`. */
    val CreditCardFilled: ImageVector by lazy {
        icon("CreditCardFilled", viewport = 24f) {
            path("M3 4h18a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-18a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2Z", fill = true, stroke = true)
            path("M1 10L23 10", stroke = true)
        }
    }

    /** StrelaTech: `filled/database.svg`. */
    val DatabaseFilled: ImageVector by lazy {
        icon("DatabaseFilled", viewport = 24f) {
            path("M12 2 C16.9706 2 21 3.34315 21 5 C21 6.65685 16.9706 8 12 8 C7.02944 8 3 6.65685 3 5 C3 3.34315 7.02944 2 12 2 Z", fill = true, evenOdd = true)
            path("M3 8.5 C4.5 9.7 8 10.5 12 10.5 C16 10.5 19.5 9.7 21 8.5 L21 12 C21 13.6569 16.9706 15 12 15 C7.02944 15 3 13.6569 3 12 Z", fill = true, evenOdd = true)
            path("M3 15.5 C4.5 16.7 8 17.5 12 17.5 C16 17.5 19.5 16.7 21 15.5 L21 19 C21 20.6569 16.9706 22 12 22 C7.02944 22 3 20.6569 3 19 Z", fill = true, evenOdd = true)
        }
    }

    /** StrelaTech: `filled/download.svg`. */
    val DownloadFilled: ImageVector by lazy {
        icon("DownloadFilled", viewport = 24f) {
            path("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", stroke = true)
            path("M7 10L12 15L17 10", stroke = true)
            path("M12 15L12 3", stroke = true)
        }
    }

    /** StrelaTech: `filled/edit.svg`. */
    val EditFilled: ImageVector by lazy {
        icon("EditFilled", viewport = 24f) {
            path("M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13", stroke = true)
            path("M18.5 2.5C18.8978 2.10218 19.4374 1.87868 20 1.87868C20.5626 1.87868 21.1022 2.10218 21.5 2.5C21.8978 2.89782 22.1213 3.43739 22.1213 4C22.1213 4.56261 21.8978 5.10218 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/eye.svg`. */
    val EyeFilled: ImageVector by lazy {
        icon("EyeFilled", viewport = 24f) {
            path("M12.0001 2.75C15.9994 2.75002 19.0572 5.0335 21.0372 7.14551C22.0384 8.21356 22.802 9.27715 23.3155 10.0723C23.5729 10.4709 23.7704 10.8048 23.9044 11.043C23.9714 11.1621 24.0229 11.2577 24.0587 11.3252C24.0764 11.3587 24.0897 11.3859 24.0997 11.4053C24.1046 11.4149 24.1094 11.4228 24.1124 11.4287C24.1139 11.4317 24.1153 11.4345 24.1163 11.4365C24.1167 11.4374 24.1169 11.4388 24.1173 11.4395V11.4404C24.1118 11.4437 24.0353 11.4824 23.0001 12C24.0353 12.5176 24.1118 12.5563 24.1173 12.5596V12.5605C24.1169 12.5612 24.1167 12.5626 24.1163 12.5635C24.1153 12.5655 24.1139 12.5683 24.1124 12.5713C24.1094 12.5772 24.1046 12.5851 24.0997 12.5947C24.0897 12.6141 24.0764 12.6413 24.0587 12.6748C24.0229 12.7423 23.9714 12.8379 23.9044 12.957C23.7704 13.1952 23.5729 13.5291 23.3155 13.9277C22.802 14.7228 22.0384 15.7864 21.0372 16.8545C19.0572 18.9665 15.9994 21.25 12.0001 21.25C8.0007 21.25 4.94298 18.9665 2.96296 16.8545C1.96171 15.7864 1.19815 14.7229 0.684636 13.9277C0.427212 13.5291 0.229759 13.1952 0.0957684 12.957C0.0287524 12.8379 -0.022799 12.7423 -0.0585285 12.6748C-0.0762827 12.6413 -0.0895823 12.6141 -0.0995441 12.5947C-0.104496 12.5851 -0.109238 12.5772 -0.112239 12.5713C-0.113736 12.5683 -0.11514 12.5655 -0.116146 12.5635L-0.117122 12.5605V12.5596C-0.111254 12.5561 -0.0322341 12.5161 1.00007 12C-0.0322341 11.4839 -0.111254 11.4439 -0.117122 11.4404V11.4395L-0.116146 11.4365C-0.11514 11.4345 -0.113736 11.4317 -0.112239 11.4287C-0.109238 11.4228 -0.104496 11.4149 -0.0995441 11.4053C-0.0895823 11.3859 -0.0762827 11.3587 -0.0585285 11.3252C-0.022799 11.2577 0.0287524 11.1621 0.0957684 11.043C0.229759 10.8048 0.427212 10.4709 0.684636 10.0723C1.19815 9.27715 1.96171 8.21356 2.96296 7.14551C4.94298 5.03349 8.0007 2.75 12.0001 2.75ZM12.0001 7.75C9.65286 7.75 7.75007 9.65279 7.75007 12C7.75007 14.3472 9.65286 16.25 12.0001 16.25C14.3472 16.25 16.2501 14.3472 16.2501 12C16.2501 9.65281 14.3472 7.75004 12.0001 7.75ZM12.0001 10.25C12.9665 10.25 13.7501 11.0335 13.7501 12C13.7501 12.9665 12.9665 13.75 12.0001 13.75C11.0336 13.75 10.2501 12.9665 10.2501 12C10.2501 11.0335 11.0336 10.25 12.0001 10.25ZM1.00007 12L-0.118099 12.5586C-0.293793 12.2069 -0.293793 11.7931 -0.118099 11.4414L1.00007 12ZM24.1182 11.4414C24.2939 11.7931 24.2939 12.2069 24.1182 12.5586L23.0001 12L24.1182 11.4414Z", fill = true)
        }
    }

    /** StrelaTech: `filled/file.svg`. */
    val FileFilled: ImageVector by lazy {
        icon("FileFilled", viewport = 24f) {
            path("M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z", stroke = true)
            path("M19 6H5V21H19V6Z", fill = true)
            path("M17 3H5V10H17V3Z", fill = true)
        }
    }

    /** StrelaTech: `filled/filter.svg`. */
    val FilterFilled: ImageVector by lazy {
        icon("FilterFilled", viewport = 24f) {
            path("M22 3L2 3L10 12.46L10 19L14 21L14 12.46L22 3Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/fingerprint.svg`. */
    val FingerprintFilled: ImageVector by lazy {
        icon("FingerprintFilled", viewport = 24f) {
            path("M1.5 11.9998C1.94799 8.86213 2.49181 7.26637 4.5 4.99976M7 2.99976C8.71302 1.31123 11.9382 1.48822 12 1.49989", stroke = true, strokeWidth = 1.5f)
            path("M15 2C16.5186 2.2355 17.1224 2.61429 18 3.5", stroke = true, strokeWidth = 1.5f)
            path("M19.5 5C21.2931 7.13732 22.0578 8.56245 22.5 12", stroke = true, strokeWidth = 1.5f)
            path("M6.5 6.5C7.80228 5.16267 8.57261 4.71716 10 4.5", stroke = true, strokeWidth = 1.5f)
            path("M13 4.5C19.6079 6.34614 21.2901 10.0494 18 20.5", stroke = true, strokeWidth = 1.5f)
            path("M16 8.49999C14.2127 7.02839 13.0716 6.67203 11 6.99999", stroke = true, strokeWidth = 1.5f)
            path("M17.5 13.5C17.8278 12.4667 17.4836 10.6267 17 10.5", stroke = true, strokeWidth = 1.5f)
            path("M17 16C16.4864 18.4794 15.9149 19.895 14 22.5", stroke = true, strokeWidth = 1.5f)
            path("M11 22.5C13.5885 18.9138 14.3625 16.51 14.5 11.5C13.9812 10.1908 13.3322 9.89191 12 9.5C10.5793 9.58472 10.1196 10.1589 9.5 11.5C9.0647 15.5784 8.47235 17.5813 6 20", stroke = true, strokeWidth = 1.5f)
            path("M12 12C11.8104 16.6401 10.8172 18.6404 8 21.5", stroke = true, strokeWidth = 1.5f)
            path("M2.5 16C3.33732 15.3141 3.72601 14.7075 4 12.5C4.02996 10.7168 4.09588 9.74708 5 8.5", stroke = true, strokeWidth = 1.5f)
            path("M4 18C5.31686 16.857 5.89432 15.9274 6.5 13.5C6.64997 11.2709 6.87483 10.1105 8 8.5", stroke = true, strokeWidth = 1.5f)
        }
    }

    /** StrelaTech: `filled/folder.svg`. */
    val FolderFilled: ImageVector by lazy {
        icon("FolderFilled", viewport = 24f) {
            path("M22 19C22 19.5304 21.7893 20.0391 21.4142 20.4142C21.0391 20.7893 20.5304 21 20 21H4C3.46957 21 2.96086 20.7893 2.58579 20.4142C2.21071 20.0391 2 19.5304 2 19V5C2 4.46957 2.21071 3.96086 2.58579 3.58579C2.96086 3.21071 3.46957 3 4 3H9L11 6H20C20.5304 6 21.0391 6.21071 21.4142 6.58579C21.7893 6.96086 22 7.46957 22 8V19Z", fill = true, stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/gauge.svg`. */
    val GaugeFilled: ImageVector by lazy {
        icon("GaugeFilled", viewport = 24f) {
            path("M4 14.89C4 12.7683 4.84285 10.7335 6.34315 9.23316C7.84344 7.73287 9.87827 6.89001 12 6.89001C14.1217 6.89001 16.1566 7.73287 17.6569 9.23316C19.1571 10.7335 20 12.7683 20 14.89", stroke = true)
            path("M12 15L15 12", stroke = true, strokeWidth = 1.5f)
            path("M12 16C12.5523 16 13 15.5523 13 15C13 14.4477 12.5523 14 12 14C11.4477 14 11 14.4477 11 15C11 15.5523 11.4477 16 12 16Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/gift.svg`. */
    val GiftFilled: ImageVector by lazy {
        icon("GiftFilled", viewport = 24f) {
            path("M2 7h20v5h-20Z", fill = true, stroke = true)
            path("M12 12L12 22", stroke = true)
            path("M4 13V21C4 21.55 4.45 22 5 22H19C19.55 22 20 21.55 20 21V13", stroke = true)
            path("M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z", fill = true, stroke = true)
            path("M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/github.svg`. */
    val GithubFilled: ImageVector by lazy {
        icon("GithubFilled", viewport = 24f) {
            path("M12 0.297C5.37 0.297 1.90735e-06 5.67 1.90735e-06 12.297C1.90735e-06 17.6 3.438 22.097 8.205 23.682C8.805 23.795 9.025 23.424 9.025 23.105C9.025 22.82 9.015 22.065 9.01 21.065C5.672 21.789 4.968 19.455 4.968 19.455C4.422 18.07 3.633 17.7 3.633 17.7C2.546 16.956 3.717 16.971 3.717 16.971C4.922 17.055 5.555 18.207 5.555 18.207C6.625 20.042 8.364 19.512 9.05 19.205C9.158 18.429 9.467 17.9 9.81 17.6C7.145 17.3 4.344 16.268 4.344 11.67C4.344 10.36 4.809 9.29 5.579 8.45C5.444 8.147 5.039 6.927 5.684 5.274C5.684 5.274 6.689 4.952 8.984 6.504C9.944 6.237 10.964 6.105 11.984 6.099C13.004 6.105 14.024 6.237 14.984 6.504C17.264 4.952 18.269 5.274 18.269 5.274C18.914 6.927 18.509 8.147 18.389 8.45C19.154 9.29 19.619 10.36 19.619 11.67C19.619 16.28 16.814 17.295 14.144 17.59C14.564 17.95 14.954 18.686 14.954 19.81C14.954 21.416 14.939 22.706 14.939 23.096C14.939 23.411 15.149 23.786 15.764 23.666C20.565 22.092 24 17.592 24 12.297C24 5.67 18.627 0.297 12 0.297Z", fill = true)
        }
    }

    /** StrelaTech: `filled/globe.svg`. */
    val GlobeFilled: ImageVector by lazy {
        icon("GlobeFilled", viewport = 24f) {
            path("M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0Z", stroke = true, strokeWidth = 2f)
            path("M3 12L21 12", stroke = true, strokeWidth = 2f)
            path("M12 3a15 15 0 0 1 0 18 15 15 0 0 1 0-18z", stroke = true, strokeWidth = 2f)
        }
    }

    /** StrelaTech: `filled/headphones.svg`. */
    val HeadphonesFilled: ImageVector by lazy {
        icon("HeadphonesFilled", viewport = 24f) {
            path("M3 18V12C3 9.61305 3.94821 7.32387 5.63604 5.63604C7.32387 3.94821 9.61305 3 12 3C14.3869 3 16.6761 3.94821 18.364 5.63604C20.0518 7.32387 21 9.61305 21 12V18", stroke = true)
            path("M21 19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H18C17.4696 21 16.9609 20.7893 16.5858 20.4142C16.2107 20.0391 16 19.5304 16 19V16C16 15.4696 16.2107 14.9609 16.5858 14.5858C16.9609 14.2107 17.4696 14 18 14H21V19ZM3 19C3 19.5304 3.21071 20.0391 3.58579 20.4142C3.96086 20.7893 4.46957 21 5 21H6C6.53043 21 7.03914 20.7893 7.41421 20.4142C7.78929 20.0391 8 19.5304 8 19V16C8 15.4696 7.78929 14.9609 7.41421 14.5858C7.03914 14.2107 6.53043 14 6 14H3V19Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/heart.svg`. */
    val HeartFilled: ImageVector by lazy {
        icon("HeartFilled", viewport = 24f) {
            path("M20.84 4.61C20.3292 4.099 19.7228 3.69365 19.0554 3.41708C18.3879 3.14052 17.6725 2.99817 16.95 2.99817C16.2275 2.99817 15.5121 3.14052 14.8446 3.41708C14.1772 3.69365 13.5708 4.099 13.06 4.61L12 5.67L10.94 4.61C9.90831 3.57831 8.50903 2.99871 7.05 2.99871C5.59097 2.99871 4.19169 3.57831 3.16 4.61C2.12831 5.64169 1.54871 7.04097 1.54871 8.5C1.54871 9.95903 2.12831 11.3583 3.16 12.39L4.22 13.45L12 21.23L19.78 13.45L20.84 12.39C21.351 11.8792 21.7564 11.2728 22.0329 10.6054C22.3095 9.9379 22.4518 9.22249 22.4518 8.5C22.4518 7.77751 22.3095 7.0621 22.0329 6.39464C21.7564 5.72719 21.351 5.12076 20.84 4.61Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/home.svg`. */
    val Home: ImageVector by lazy {
        icon("Home", viewport = 24f) {
            path("M3 10.5L12 3l9 7.5", fill = true, stroke = true)
            path("M5 9.5V20a1 1 0 0 0 1 1h4v-6h4v6h4a1 1 0 0 0 1-1V9.5", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `outline/image.svg` (маска заменена outline-вариантом). */
    val ImageFilled: ImageVector by lazy {
        icon("ImageFilled", viewport = 24f) {
            path("M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", stroke = true)
            path("M7 8.5a1.5 1.5 0 1 0 3 0a1.5 1.5 0 1 0 -3 0Z", stroke = true)
            path("M21 15l-5-5L5 21", stroke = true)
        }
    }

    /** StrelaTech: `filled/info.svg`. */
    val InfoFilled: ImageVector by lazy {
        icon("InfoFilled", viewport = 24f) {
            path("M12 2C17.5228 2 22 6.47715 22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2ZM12 10C11.4477 10 11 10.4477 11 11V16C11 16.5523 11.4477 17 12 17C12.5523 17 13 16.5523 13 16V11C13 10.4477 12.5523 10 12 10ZM12 6.5C11.1716 6.5 10.5 7.17157 10.5 8C10.5 8.82843 11.1716 9.5 12 9.5C12.8284 9.5 13.5 8.82843 13.5 8C13.5 7.17157 12.8284 6.5 12 6.5Z", fill = true)
        }
    }

    /** StrelaTech: `filled/key.svg`. */
    val KeyFilled: ImageVector by lazy {
        icon("KeyFilled", viewport = 24f) {
            path("M2 15.5a5.5 5.5 0 1 0 11 0a5.5 5.5 0 1 0 -11 0Z", fill = true, stroke = true)
            path("M11.5 11.5L21 2", stroke = true)
            path("M16 7L20 11", stroke = true)
            path("M19 4L22 7", stroke = true)
        }
    }

    /** StrelaTech: `filled/link.svg`. */
    val LinkFilled: ImageVector by lazy {
        icon("LinkFilled", viewport = 24f) {
            path("M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71", stroke = true)
            path("M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71", stroke = true)
        }
    }

    /** StrelaTech: `filled/lock.svg`. */
    val LockFilled: ImageVector by lazy {
        icon("LockFilled", viewport = 24f) {
            path("M18 11H6C4.89543 11 4 11.8954 4 13V19C4 20.1046 4.89543 21 6 21H18C19.1046 21 20 20.1046 20 19V13C20 11.8954 19.1046 11 18 11Z", fill = true, stroke = true, strokeWidth = 3f)
            path("M8 11V7C8 5.93913 8.42143 4.92172 9.17157 4.17157C9.92172 3.42143 10.9391 3 12 3C13.0609 3 14.0783 3.42143 14.8284 4.17157C15.5786 4.92172 16 5.93913 16 7V11", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/mail.svg`. */
    val MailFilled: ImageVector by lazy {
        icon("MailFilled", viewport = 24f) {
            path("M19.9997 2.75C21.7947 2.75 23.2497 4.20507 23.2497 6V18C23.2497 19.7949 21.7947 21.25 19.9997 21.25H3.99974C2.20494 21.2499 0.749745 19.7948 0.749745 18V6C0.749745 4.20516 2.20494 2.75014 3.99974 2.75H19.9997ZM23.0242 6.2832C22.6283 5.71781 21.8485 5.57989 21.2829 5.97559L11.9997 12.4736L2.71654 5.97559C2.15101 5.58001 1.37114 5.71776 0.975331 6.2832C0.57975 6.84868 0.717635 7.62856 1.28295 8.02441L11.2829 15.0244C11.7132 15.3255 12.2862 15.3254 12.7165 15.0244L22.7165 8.02441C23.282 7.62857 23.4199 6.84875 23.0242 6.2832Z", fill = true)
        }
    }

    /** StrelaTech: `filled/map-pin.svg`. */
    val MapPinFilled: ImageVector by lazy {
        icon("MapPinFilled", viewport = 24f) {
            path("M12 -0.200195C14.7052 -0.200195 17.3 0.874238 19.2129 2.78711C21.1258 4.69998 22.2002 7.29479 22.2002 10C22.2002 13.9823 19.6688 17.5248 17.3711 19.9502C16.1977 21.1888 15.028 22.1951 14.1533 22.8916C13.7151 23.2405 13.3483 23.5138 13.0889 23.7012C12.9592 23.7948 12.8562 23.8671 12.7842 23.917C12.7481 23.9419 12.7202 23.962 12.7002 23.9756C12.6904 23.9822 12.6825 23.9874 12.6768 23.9912C12.6739 23.9931 12.6707 23.9949 12.6689 23.9961L12.667 23.9971L12.666 23.998C12.3135 24.2331 11.8672 24.263 11.4912 24.0869L11.334 23.998L11.333 23.9971L11.3311 23.9961C11.3293 23.9949 11.3261 23.9931 11.3232 23.9912C11.3175 23.9874 11.3096 23.9822 11.2998 23.9756C11.2798 23.962 11.2519 23.9419 11.2158 23.917C11.1438 23.8671 11.0408 23.7948 10.9111 23.7012C10.6517 23.5138 10.2849 23.2405 9.84668 22.8916C8.97204 22.1951 7.80232 21.1888 6.62891 19.9502C4.33117 17.5248 1.7998 13.9823 1.7998 10C1.7998 7.29479 2.87424 4.69998 4.78711 2.78711C6.69998 0.874238 9.29479 -0.200195 12 -0.200195ZM12 7C10.3431 7 9 8.34315 9 10C9 11.6569 10.3431 13 12 13C13.6569 13 15 11.6569 15 10C15 8.34315 13.6569 7 12 7Z", fill = true)
        }
    }

    /** StrelaTech: `filled/menu.svg`. */
    val MenuFilled: ImageVector by lazy {
        icon("MenuFilled", viewport = 24f) {
            path("M4 6L20 6", stroke = true, strokeWidth = 3f)
            path("M4 12L20 12", stroke = true, strokeWidth = 3f)
            path("M4 18L20 18", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/mic.svg`. */
    val MicFilled: ImageVector by lazy {
        icon("MicFilled", viewport = 24f) {
            path("M15 4C15 2.34315 13.6569 1 12 1C10.3431 1 9 2.34315 9 4V9C9 10.6569 10.3431 12 12 12C13.6569 12 15 10.6569 15 9V4Z", fill = true, stroke = true)
            path("M5 10C5 11.8565 5.7375 13.637 7.05025 14.9497C8.36301 16.2625 10.1435 17 12 17C13.8565 17 15.637 16.2625 16.9497 14.9497C18.2625 13.637 19 11.8565 19 10", stroke = true)
            path("M12 19V22", stroke = true)
        }
    }

    /** StrelaTech: `filled/minus.svg`. */
    val MinusFilled: ImageVector by lazy {
        icon("MinusFilled", viewport = 24f) {
            path("M5 12L19 12", stroke = true)
        }
    }

    /** StrelaTech: `filled/monitor.svg`. */
    val MonitorFilled: ImageVector by lazy {
        icon("MonitorFilled", viewport = 24f) {
            path("M20 3H4C2.89543 3 2 3.89543 2 5V15C2 16.1046 2.89543 17 4 17H20C21.1046 17 22 16.1046 22 15V5C22 3.89543 21.1046 3 20 3Z", fill = true, stroke = true)
            path("M8 21H16", stroke = true)
            path("M12 17V21", stroke = true)
        }
    }

    /** StrelaTech: `filled/moon.svg`. */
    val MoonFilled: ImageVector by lazy {
        icon("MoonFilled", viewport = 24f) {
            path("M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/music.svg`. */
    val MusicFilled: ImageVector by lazy {
        icon("MusicFilled", viewport = 24f) {
            path("M9 18V5L21 3V16", stroke = true)
            path("M6 21C7.65685 21 9 19.6569 9 18C9 16.3431 7.65685 15 6 15C4.34315 15 3 16.3431 3 18C3 19.6569 4.34315 21 6 21Z", fill = true, stroke = true)
            path("M18 19C19.6569 19 21 17.6569 21 16C21 14.3431 19.6569 13 18 13C16.3431 13 15 14.3431 15 16C15 17.6569 16.3431 19 18 19Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/onebox-logo.svg`. */
    val OneBoxLogoFilled: ImageVector by lazy {
        icon("OneBoxLogoFilled", viewport = 512f) {
            path("M112.155 204.197C112.131 195.622 121.347 190.221 128.65 194.529L247.4 264.587C250.725 266.548 252.769 270.144 252.78 274.048L253.168 413.481C253.192 422.056 243.976 427.457 236.673 423.149L117.923 353.091C114.599 351.13 112.554 347.534 112.543 343.63L112.155 204.197Z", fill = true)
            path("M132.35 191.509C125.057 187.267 125.057 176.733 132.35 172.491L251.47 103.216C254.888 101.228 259.111 101.228 262.53 103.216L381.649 172.491C388.942 176.733 388.942 187.267 381.649 191.509L262.53 260.784C259.111 262.772 254.888 262.772 251.47 260.784L132.35 191.509Z", fill = true)
            path("M398.844 204.197C398.868 195.622 389.653 190.221 382.35 194.529L263.6 264.587C260.275 266.548 258.23 270.144 258.219 274.048L257.832 413.481C257.808 422.056 267.023 427.457 274.326 423.149L393.076 353.091C396.401 351.13 398.446 347.534 398.457 343.63L398.844 204.197Z", fill = true)
        }
    }

    /** StrelaTech: `filled/pause.svg`. */
    val PauseFilled: ImageVector by lazy {
        icon("PauseFilled", viewport = 24f) {
            path("M9 4H7C6.44772 4 6 4.44772 6 5V19C6 19.5523 6.44772 20 7 20H9C9.55228 20 10 19.5523 10 19V5C10 4.44772 9.55228 4 9 4Z", fill = true, stroke = true)
            path("M17 4H15C14.4477 4 14 4.44772 14 5V19C14 19.5523 14.4477 20 15 20H17C17.5523 20 18 19.5523 18 19V5C18 4.44772 17.5523 4 17 4Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/phone.svg`. */
    val PhoneFilled: ImageVector by lazy {
        icon("PhoneFilled", viewport = 24f) {
            path("M22 16.92V19.92C22.0011 20.1985 21.9441 20.4742 21.8325 20.7293C21.7209 20.9845 21.5573 21.2136 21.3521 21.4019C21.1469 21.5901 20.9046 21.7335 20.6407 21.8227C20.3769 21.9119 20.0974 21.9451 19.82 21.92C16.7428 21.5856 13.787 20.5341 11.19 18.85C8.77383 17.3147 6.72534 15.2662 5.19 12.85C3.49998 10.2412 2.44824 7.27099 2.12 4.18C2.09501 3.90347 2.12787 3.62476 2.2165 3.36162C2.30513 3.09849 2.44757 2.85669 2.63477 2.65162C2.82196 2.44655 3.0498 2.28271 3.30379 2.17052C3.55778 2.05833 3.83234 2.00026 4.11 2H7.11C7.59531 1.99522 8.06579 2.16708 8.43376 2.48353C8.80173 2.79999 9.04208 3.23945 9.11 3.72C9.24 4.68 9.47 5.62 9.81 6.53C9.94454 6.88792 9.97366 7.27691 9.89391 7.65088C9.81415 8.02485 9.62886 8.36811 9.36 8.64L8.09 9.91C9.51356 12.4135 11.5865 14.4864 14.09 15.91L15.36 14.64C15.6319 14.3711 15.9752 14.1858 16.3491 14.1061C16.7231 14.0263 17.1121 14.0555 17.47 14.19C18.38 14.53 19.32 14.76 20.28 14.89C20.7658 14.9585 21.2094 15.2032 21.5265 15.5775C21.8437 15.9518 22.0122 16.4296 22 16.92Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/play.svg`. */
    val PlayFilled: ImageVector by lazy {
        icon("PlayFilled", viewport = 24f) {
            path("M6 3L20 12L6 21V3Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/plus.svg`. */
    val PlusFilled: ImageVector by lazy {
        icon("PlusFilled", viewport = 24f) {
            path("M12 5L12 19", stroke = true)
            path("M5 12L19 12", stroke = true)
        }
    }

    /** StrelaTech: `filled/power.svg`. */
    val PowerFilled: ImageVector by lazy {
        icon("PowerFilled", viewport = 24f) {
            path("M18.36 6.64a9 9 0 1 1-12.73 0", stroke = true)
            path("M12 2L12 12", stroke = true)
        }
    }

    /** StrelaTech: `filled/python.svg`. */
    val PythonFilled: ImageVector by lazy {
        icon("PythonFilled", viewport = 24f) {
            path("M11.914 0C5.82 0 6.2 2.656 6.2 2.656L6.207 5.408H12.021V6.234H3.9C3.9 6.234 0 5.789 0 11.969C0 18.149 3.403 17.929 3.403 17.929H5.433V15.062C5.433 15.062 5.324 11.659 8.783 11.659H14.548C14.548 11.659 17.788 11.711 17.788 8.527V3.135C17.788 3.135 18.293 0 11.914 0ZM8.69 1.887C8.96848 1.887 9.23555 1.99762 9.43246 2.19454C9.62937 2.39145 9.74 2.65852 9.74 2.937C9.74 3.21548 9.62937 3.48255 9.43246 3.67946C9.23555 3.87638 8.96848 3.987 8.69 3.987C8.41152 3.987 8.14445 3.87638 7.94754 3.67946C7.75062 3.48255 7.64 3.21548 7.64 2.937C7.64 2.65852 7.75062 2.39145 7.94754 2.19454C8.14445 1.99762 8.41152 1.887 8.69 1.887Z", fill = true)
            path("M12.086 24C18.18 24 17.8 21.344 17.8 21.344L17.793 18.592H11.979V17.766H20.1C20.1 17.766 24 18.211 24 12.031C24 5.851 20.597 6.071 20.597 6.071H18.567V8.938C18.567 8.938 18.676 12.341 15.217 12.341H9.452C9.452 12.341 6.212 12.289 6.212 15.473V20.865C6.212 20.865 5.707 24 12.086 24ZM15.31 22.113C15.0315 22.113 14.7645 22.0024 14.5675 21.8055C14.3706 21.6085 14.26 21.3415 14.26 21.063C14.26 20.7845 14.3706 20.5175 14.5675 20.3205C14.7645 20.1236 15.0315 20.013 15.31 20.013C15.5885 20.013 15.8555 20.1236 16.0525 20.3205C16.2494 20.5175 16.36 20.7845 16.36 21.063C16.36 21.3415 16.2494 21.6085 16.0525 21.8055C15.8555 22.0024 15.5885 22.113 15.31 22.113Z", fill = true)
        }
    }

    /** StrelaTech: `filled/refresh.svg`. */
    val RefreshFilled: ImageVector by lazy {
        icon("RefreshFilled", viewport = 24f) {
            path("M1 4L1 10L7 10", stroke = true, strokeWidth = 3f)
            path("M3.51 15a9 9 0 1 0 2.13-9.36L1 10", stroke = true, strokeWidth = 3f)
        }
    }

    /** StrelaTech: `filled/resize.svg`. */
    val ResizeFilled: ImageVector by lazy {
        icon("ResizeFilled", viewport = 24f) {
            path("M15 3L21 3L21 9", stroke = true)
            path("M9 21L3 21L3 15", stroke = true)
            path("M21 3L14 10", stroke = true)
            path("M3 21L10 14", stroke = true)
        }
    }

    /** StrelaTech: `filled/scale.svg`. */
    val ScaleFilled: ImageVector by lazy {
        icon("ScaleFilled", viewport = 24f) {
            path("M12 3V21", stroke = true)
            path("M5 21H19", stroke = true)
            path("M5 7H19Z", fill = true)
            path("M5 7H19", stroke = true)
            path("M8 11C8 11.7956 7.68393 12.5587 7.12132 13.1213C6.55871 13.6839 5.79565 14 5 14C4.20435 14 3.44129 13.6839 2.87868 13.1213C2.31607 12.5587 2 11.7956 2 11", stroke = true)
            path("M22 11C22 11.7956 21.6839 12.5587 21.1213 13.1213C20.5587 13.6839 19.7956 14 19 14C18.2044 14 17.4413 13.6839 16.8787 13.1213C16.3161 12.5587 16 11.7956 16 11", stroke = true)
        }
    }

    /** StrelaTech: `filled/search.svg`. */
    val SearchFilled: ImageVector by lazy {
        icon("SearchFilled", viewport = 24f) {
            path("M4 11a7 7 0 1 0 14 0a7 7 0 1 0 -14 0Z", stroke = true)
            path("M16.5 16.5L21 21", stroke = true)
        }
    }

    /** StrelaTech: `filled/send.svg`. */
    val SendFilled: ImageVector by lazy {
        icon("SendFilled", viewport = 24f) {
            path("M22 2L15 22L11 13L2 9L22 2Z", fill = true, stroke = true)
            path("M22 2L11 13", stroke = true)
        }
    }

    /** StrelaTech: `filled/server.svg`. */
    val ServerFilled: ImageVector by lazy {
        icon("ServerFilled", viewport = 24f) {
            path("M4 2h16a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-4a2 2 0 0 1 2 -2Z", fill = true, stroke = true)
            path("M4 14h16a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-4a2 2 0 0 1 2 -2Z", fill = true, stroke = true)
            path("M5.4 6a0.6 0.6 0 1 0 1.2 0a0.6 0.6 0 1 0 -1.2 0Z", fill = true, stroke = true)
            path("M5.4 18a0.6 0.6 0 1 0 1.2 0a0.6 0.6 0 1 0 -1.2 0Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/settings.svg`. */
    val Settings: ImageVector by lazy {
        icon("Settings", viewport = 24f) {
            path("M12 -0.25C12.862 -0.25 13.6884 0.0926554 14.2979 0.702148C14.9073 1.31164 15.25 2.13805 15.25 3V3.08496L15.2549 3.14258C15.2635 3.20011 15.284 3.2558 15.3164 3.30469C15.3487 3.35336 15.391 3.39449 15.4404 3.4248L15.4922 3.45117L15.5049 3.45605C15.5779 3.48828 15.6588 3.49855 15.7373 3.48438C15.8159 3.47012 15.8892 3.43186 15.9463 3.37598L15.9961 3.32617C16.6058 2.71649 17.4327 2.37404 18.2949 2.37402C18.7218 2.37402 19.1447 2.45776 19.5391 2.62109C19.9335 2.78447 20.2919 3.0243 20.5938 3.32617C20.8956 3.62806 21.1354 3.98641 21.2988 4.38086C21.4622 4.77528 21.5459 5.19809 21.5459 5.625C21.5459 6.05191 21.4622 6.47472 21.2988 6.86914C21.1354 7.26359 20.8956 7.62194 20.5938 7.92383L20.5439 7.97363L20.5049 8.01953C20.4701 8.06772 20.4462 8.12365 20.4355 8.18262C20.4214 8.26114 20.4317 8.34203 20.4639 8.41504C20.4907 8.47594 20.5106 8.53945 20.5273 8.60352C20.5525 8.63426 20.5827 8.66141 20.6162 8.68359C20.6812 8.7265 20.7571 8.74968 20.835 8.75H21C21.862 8.75 22.6884 9.09266 23.2979 9.70215C23.9073 10.3116 24.25 11.138 24.25 12C24.25 12.862 23.9073 13.6884 23.2979 14.2979C22.6884 14.9073 21.862 15.25 21 15.25H20.915L20.8574 15.2549C20.7999 15.2635 20.7442 15.284 20.6953 15.3164C20.6304 15.3594 20.5795 15.4206 20.5488 15.4922L20.5439 15.5049C20.5117 15.5779 20.5015 15.6588 20.5156 15.7373C20.5299 15.8159 20.5681 15.8892 20.624 15.9463L20.6738 15.9961C20.9757 16.298 21.2155 16.6563 21.3789 17.0508C21.5423 17.4452 21.626 17.868 21.626 18.2949C21.626 18.7218 21.5422 19.1447 21.3789 19.5391C21.2155 19.9335 20.9757 20.2919 20.6738 20.5938C20.3719 20.8956 20.0136 21.1354 19.6191 21.2988C19.2247 21.4622 18.8019 21.5459 18.375 21.5459C17.9481 21.5459 17.5253 21.4622 17.1309 21.2988C16.7364 21.1354 16.3781 20.8956 16.0762 20.5938L16.0264 20.5439L15.9805 20.5049C15.9323 20.4701 15.8763 20.4462 15.8174 20.4355C15.7389 20.4214 15.658 20.4317 15.585 20.4639L15.5723 20.4688C15.5006 20.4995 15.4395 20.5512 15.3965 20.6162C15.3536 20.6812 15.3304 20.7571 15.3301 20.835V21C15.3301 21.8619 14.9874 22.6884 14.3779 23.2979C13.7685 23.9073 12.942 24.25 12.0801 24.25C11.2181 24.25 10.3917 23.9073 9.78223 23.2979C9.17273 22.6884 8.83008 21.862 8.83008 21V20.9395L8.82422 20.8799C8.81395 20.821 8.79091 20.7648 8.75586 20.7158C8.70917 20.6505 8.64368 20.6009 8.56836 20.5732C8.54368 20.5642 8.51917 20.5546 8.49512 20.5439C8.4221 20.5117 8.34123 20.5015 8.2627 20.5156C8.18405 20.5299 8.11084 20.5681 8.05371 20.624L8.00391 20.6738C7.39422 21.2835 6.5673 21.626 5.70508 21.626C4.84285 21.626 4.01595 21.2835 3.40625 20.6738C2.79655 20.0641 2.4541 19.2372 2.4541 18.375C2.4541 17.5128 2.79655 16.6859 3.40625 16.0762L3.46094 16.0195C3.51386 15.9633 3.55066 15.8935 3.56445 15.8174C3.57864 15.7389 3.56833 15.658 3.53613 15.585L3.53125 15.5723C3.50057 15.5007 3.44963 15.4395 3.38477 15.3965C3.33586 15.3641 3.28021 15.3426 3.22266 15.334L3.16504 15.3301H3C2.13806 15.3301 1.31164 14.9874 0.702148 14.3779C0.0926739 13.7685 -0.249979 12.942 -0.25 12.0801C-0.25 11.2181 0.0926798 10.3917 0.702148 9.78223C1.31164 9.17273 2.13805 8.83008 3 8.83008H3.06055C3.14079 8.8282 3.2189 8.80255 3.28418 8.75586C3.34946 8.70917 3.39905 8.64368 3.42676 8.56836C3.43584 8.54368 3.44544 8.51917 3.45605 8.49512C3.48828 8.4221 3.49855 8.34123 3.48438 8.2627C3.47012 8.18405 3.43186 8.11084 3.37598 8.05371L3.32617 8.00391C3.02428 7.70201 2.78448 7.34366 2.62109 6.94922C2.45773 6.55481 2.37403 6.13198 2.37402 5.70508C2.37402 5.2782 2.45776 4.85533 2.62109 4.46094C2.78447 4.06651 3.0243 3.70814 3.32617 3.40625C3.62807 3.10436 3.98642 2.86456 4.38086 2.70117C4.77528 2.53781 5.19808 2.4541 5.625 2.4541C6.05192 2.4541 6.47472 2.53781 6.86914 2.70117C7.26358 2.86456 7.62193 3.10436 7.92383 3.40625L7.97363 3.45605C8.03077 3.51194 8.10398 3.55019 8.18262 3.56445C8.26114 3.57864 8.34203 3.56833 8.41504 3.53613C8.47601 3.50922 8.53937 3.48848 8.60352 3.47168C8.63397 3.44663 8.66156 3.41797 8.68359 3.38477C8.72576 3.32109 8.74876 3.24622 8.75 3.16992V3C8.75 2.13805 9.09266 1.31164 9.70215 0.702148C10.3116 0.0926554 11.138 -0.25 12 -0.25ZM12 7.75C9.65279 7.75 7.75 9.65279 7.75 12C7.75 14.3472 9.65279 16.25 12 16.25C14.3472 16.25 16.25 14.3472 16.25 12C16.25 9.65279 14.3472 7.75 12 7.75Z", fill = true)
        }
    }

    /** StrelaTech: `filled/share.svg`. */
    val ShareFilled: ImageVector by lazy {
        icon("ShareFilled", viewport = 24f) {
            path("M18 8C19.6569 8 21 6.65685 21 5C21 3.34315 19.6569 2 18 2C16.3431 2 15 3.34315 15 5C15 6.65685 16.3431 8 18 8Z", fill = true, stroke = true)
            path("M6 15C7.65685 15 9 13.6569 9 12C9 10.3431 7.65685 9 6 9C4.34315 9 3 10.3431 3 12C3 13.6569 4.34315 15 6 15Z", fill = true, stroke = true)
            path("M18 22C19.6569 22 21 20.6569 21 19C21 17.3431 19.6569 16 18 16C16.3431 16 15 17.3431 15 19C15 20.6569 16.3431 22 18 22Z", fill = true, stroke = true)
            path("M8.59 10.49L15.42 6.51", stroke = true)
            path("M8.59 13.51L15.42 17.49", stroke = true)
        }
    }

    /** StrelaTech: `filled/shield.svg`. */
    val ShieldFilled: ImageVector by lazy {
        icon("ShieldFilled", viewport = 24f) {
            path("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/shopping-cart.svg`. */
    val ShoppingCartFilled: ImageVector by lazy {
        icon("ShoppingCartFilled", viewport = 24f) {
            path("M8 21a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", fill = true, stroke = true)
            path("M19 21a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", fill = true, stroke = true)
            path("M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/smartphone.svg`. */
    val SmartphoneFilled: ImageVector by lazy {
        icon("SmartphoneFilled", viewport = 24f) {
            path("M17 -0.25C18.7949 -0.25 20.25 1.20507 20.25 3V21C20.25 22.7949 18.7949 24.25 17 24.25H7C5.20507 24.25 3.75 22.7949 3.75 21V3C3.75 1.20507 5.20507 -0.25 7 -0.25H17ZM12 16.75C11.3096 16.75 10.75 17.3096 10.75 18V18.0098C10.75 18.7001 11.3096 19.2598 12 19.2598C12.6904 19.2598 13.25 18.7001 13.25 18.0098V18C13.25 17.3096 12.6904 16.75 12 16.75Z", fill = true)
        }
    }

    /** StrelaTech: `filled/star.svg`. */
    val StarFilled: ImageVector by lazy {
        icon("StarFilled", viewport = 24f) {
            path("M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/strela-logo.svg`. */
    val StrelaLogoFilled: ImageVector by lazy {
        icon("StrelaLogoFilled", viewport = 24f) {
            path("M0.7 12a11.3 11.3 0 1 0 22.6 0a11.3 11.3 0 1 0 -22.6 0Z", stroke = true, strokeWidth = 1.4f)
            path("M8.7 12a3.3 3.3 0 1 0 6.6 0a3.3 3.3 0 1 0 -6.6 0Z", stroke = true, strokeWidth = 1.4f)
            path("M11.2 12a0.8 0.8 0 1 0 1.6 0a0.8 0.8 0 1 0 -1.6 0Z", fill = true, stroke = true, strokeWidth = 1.4f)
            path("M14.75 4.48486C17.8137 5.60623 20 8.54796 20 12C20 16.4183 16.4183 20 12 20C7.58172 20 4 16.4183 4 12C4 8.54796 6.18636 5.60623 9.25 4.48486V5.99854C6.97788 7.04142 5.3999 9.33642 5.3999 12C5.3999 15.6451 8.3549 18.6001 12 18.6001C15.6451 18.6001 18.6001 15.6451 18.6001 12C18.6001 9.33642 17.0221 7.04142 14.75 5.99854V4.48486Z", fill = true)
            path("M13.9773 3.78977L13.0398 3.89204C13.0132 3.79735 12.9668 3.70834 12.9006 3.625C12.8361 3.54167 12.749 3.47443 12.6392 3.4233C12.5293 3.37216 12.3949 3.34659 12.2358 3.34659C12.0218 3.34659 11.8418 3.39299 11.696 3.48579C11.5521 3.5786 11.4811 3.69886 11.483 3.84659C11.4811 3.97348 11.5274 4.0767 11.6222 4.15625C11.7188 4.23579 11.8778 4.30114 12.0994 4.35227L12.8438 4.51136C13.2567 4.60038 13.5634 4.74148 13.7642 4.93466C13.9668 5.12784 14.0691 5.38068 14.071 5.69318C14.0691 5.9678 13.9886 6.21023 13.8295 6.42045C13.6723 6.62879 13.4536 6.79167 13.1733 6.90909C12.893 7.02651 12.571 7.08523 12.2074 7.08523C11.6733 7.08523 11.2434 6.97348 10.9176 6.75C10.5919 6.52462 10.3977 6.21117 10.3352 5.80966L11.3381 5.71307C11.3835 5.91004 11.4801 6.05871 11.6278 6.15909C11.7756 6.25947 11.9678 6.30966 12.2045 6.30966C12.4489 6.30966 12.6449 6.25947 12.7926 6.15909C12.9422 6.05871 13.017 5.93466 13.017 5.78693C13.017 5.66193 12.9688 5.55871 12.8722 5.47727C12.7775 5.39583 12.6297 5.33333 12.429 5.28977L11.6847 5.13352C11.2661 5.0464 10.9564 4.89962 10.7557 4.69318C10.5549 4.48485 10.4555 4.22159 10.4574 3.90341C10.4555 3.63447 10.5284 3.40152 10.6761 3.20455C10.8258 3.00568 11.0331 2.85227 11.2983 2.74432C11.5653 2.63447 11.8731 2.57955 12.2216 2.57955C12.7329 2.57955 13.1354 2.68845 13.429 2.90625C13.7244 3.12405 13.9072 3.41856 13.9773 3.78977Z", fill = true)
        }
    }

    /** StrelaTech: `filled/sun.svg`. */
    val SunFilled: ImageVector by lazy {
        icon("SunFilled", viewport = 24f) {
            path("M7 12a5 5 0 1 0 10 0a5 5 0 1 0 -10 0Z", fill = true, stroke = true)
            path("M12 1L12 3", stroke = true)
            path("M12 21L12 23", stroke = true)
            path("M4.22 4.22L5.64 5.64", stroke = true)
            path("M18.36 18.36L19.78 19.78", stroke = true)
            path("M1 12L3 12", stroke = true)
            path("M21 12L23 12", stroke = true)
            path("M4.22 19.78L5.64 18.36", stroke = true)
            path("M18.36 5.64L19.78 4.22", stroke = true)
        }
    }

    /** StrelaTech: `filled/tag.svg`. */
    val TagFilled: ImageVector by lazy {
        icon("TagFilled", viewport = 24f) {
            path("M12 0.75C12.3315 0.75 12.6494 0.881791 12.8838 1.11621L21.4766 9.70898C22.0818 10.3179 22.4219 11.1415 22.4219 12C22.4219 12.8585 22.0818 13.6821 21.4766 14.291L21.4736 14.2939L14.3037 21.4629C14.002 21.7649 13.6444 22.0054 13.25 22.1689C12.8555 22.3325 12.432 22.417 12.0049 22.417C11.5779 22.417 11.1552 22.3324 10.7607 22.1689C10.3668 22.0056 10.0086 21.7663 9.70703 21.4648L1.11621 12.8848C0.881492 12.6503 0.75 12.3318 0.75 12V2C0.75 1.30964 1.30964 0.75 2 0.75H12ZM7 4.75C5.75736 4.75 4.75 5.75736 4.75 7C4.75 8.24264 5.75736 9.25 7 9.25C8.24264 9.25 9.25 8.24264 9.25 7C9.25 5.75736 8.24264 4.75 7 4.75Z", fill = true)
        }
    }

    /** StrelaTech: `filled/terminal.svg`. */
    val TerminalFilled: ImageVector by lazy {
        icon("TerminalFilled", viewport = 24f) {
            path("M4 17L10 11L4 5", stroke = true)
            path("M12 19L20 19", stroke = true)
        }
    }

    /** StrelaTech: `filled/timer.svg`. */
    val TimerFilled: ImageVector by lazy {
        icon("TimerFilled", viewport = 24f) {
            path("M4 13a8 8 0 1 0 16 0a8 8 0 1 0 -16 0Z", fill = true, stroke = true)
            path("M12 10L12 13", stroke = true)
            path("M10 2L14 2", stroke = true)
        }
    }

    /** StrelaTech: `filled/trash.svg`. */
    val TrashFilled: ImageVector by lazy {
        icon("TrashFilled", viewport = 24f) {
            path("M3 6L5 6L21 6", fill = true, stroke = true)
            path("M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6", fill = true, stroke = true)
            path("M10 11v6", fill = true, stroke = true)
            path("M14 11v6", fill = true, stroke = true)
            path("M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/upload.svg`. */
    val UploadFilled: ImageVector by lazy {
        icon("UploadFilled", viewport = 24f) {
            path("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", stroke = true)
            path("M17 8L12 3L7 8", stroke = true)
            path("M12 3L12 15", stroke = true)
        }
    }

    /** StrelaTech: `filled/user.svg`. */
    val UserFilled: ImageVector by lazy {
        icon("UserFilled", viewport = 24f) {
            path("M12 12C14.2091 12 16 10.2091 16 8C16 5.79086 14.2091 4 12 4C9.79086 4 8 5.79086 8 8C8 10.2091 9.79086 12 12 12Z", fill = true, stroke = true)
            path("M4 21V20C4 18.4087 4.63214 16.8826 5.75736 15.7574C6.88258 14.6321 8.4087 14 10 14H14C15.5913 14 17.1174 14.6321 18.2426 15.7574C19.3679 16.8826 20 18.4087 20 20V21", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/video.svg`. */
    val VideoFilled: ImageVector by lazy {
        icon("VideoFilled", viewport = 24f) {
            path("M14 5H3C1.89543 5 1 5.89543 1 7V17C1 18.1046 1.89543 19 3 19H14C15.1046 19 16 18.1046 16 17V7C16 5.89543 15.1046 5 14 5Z", fill = true, stroke = true)
            path("M16 10L23 7V17L16 14V10Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/volume.svg`. */
    val VolumeFilled: ImageVector by lazy {
        icon("VolumeFilled", viewport = 24f) {
            path("M11 5L6 9H2V15H6L11 19V5Z", fill = true, stroke = true)
            path("M15.54 8.46C16.4774 9.39764 17.0039 10.6692 17.0039 11.995C17.0039 13.3208 16.4774 14.5924 15.54 15.53", stroke = true)
            path("M19.07 4.93C20.9447 6.80528 21.9979 9.34836 21.9979 12C21.9979 14.6516 20.9447 17.1947 19.07 19.07", stroke = true)
        }
    }

    /** StrelaTech: `filled/wifi.svg`. */
    val WifiFilled: ImageVector by lazy {
        icon("WifiFilled", viewport = 24f) {
            path("M5 12.55a11 11 0 0 1 14.08 0", stroke = true)
            path("M1.42 9a16 16 0 0 1 21.16 0", stroke = true)
            path("M8.53 16.11a6 6 0 0 1 6.95 0", stroke = true)
            path("M11 20a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z", fill = true, stroke = true)
        }
    }

    /** StrelaTech: `filled/zap.svg`. */
    val ZapFilled: ImageVector by lazy {
        icon("ZapFilled", viewport = 24f) {
            path("M13 2L3 14H12L11 22L21 10H12L13 2Z", fill = true, stroke = true)
        }
    }

    // Псевдонимы для имён из прежнего набора Material Symbols.
    val Clipboard: ImageVector get() = File
    val FormatSize: ImageVector get() = Resize
    val Layers: ImageVector get() = Database
}

private fun icon(
    name: String,
    viewport: Float,
    build: ImageVector.Builder.() -> Unit,
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewport,
        viewportHeight = viewport,
    ).apply(build).build()

private fun ImageVector.Builder.path(
    d: String,
    fill: Boolean = false,
    stroke: Boolean = false,
    strokeWidth: Float = 2.5f,
    evenOdd: Boolean = false,
) {
    addPath(
        pathData = PathParser().parsePathString(d).toNodes(),
        pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
        fill = if (fill) SolidColor(Color.Black) else null,
        stroke = if (stroke) SolidColor(Color.Black) else null,
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}
