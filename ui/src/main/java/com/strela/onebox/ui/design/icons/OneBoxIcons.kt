package com.strela.onebox.ui.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Иконки OneBox — официальные [Google Material Symbols](https://fonts.google.com/icons).
 * SVG-исходники лежат в `app/src/main/res/icons/{outline,filled}` и скачаны с
 * fonts.gstatic.com (стиль outlined / fill1, вес 400, размер 24).
 *
 * Для нижней навигации у Home/Globe/Settings есть пары: outline-варианты
 * (`HomeOutline`, `Globe`, `SettingsOutline`) рисуются для невыбранной вкладки,
 * fill-варианты (`Home`, `GlobeFilled`, `Settings`) — для выбранной.
 * Цвет задаётся при отрисовке через `Icon(tint = ...)`, поэтому в самих векторах
 * используется чёрный.
 */
object OneBoxIcons {

    /** Google Material Symbols: `warning` (outline, 24 px). */
    val Alert: ImageVector by lazy {
        material(
            name = "Alert",
            d = "m40 -120 440 -760 440 760H40Zm138 -80h604L480 -720 178 -200Zm330.5 -51.5Q520 -263 520 -280t-11.5 -28.5Q497 -320 480 -320t-28.5 11.5Q440 -297 440 -280t11.5 28.5Q463 -240 480 -240t28.5 -11.5ZM440 -360h80v-200h-80v200Zm40 -100Z",
        )
    }

    /** Google Material Symbols: `arrow_downward` (outline, 24 px). */
    val ArrowDown: ImageVector by lazy {
        material(
            name = "ArrowDown",
            d = "M440 -800v487L216 -537l-56 57 320 320 320 -320 -56 -57 -224 224v-487h-80Z",
        )
    }

    /** Google Material Symbols: `arrow_back` (outline, 24 px). */
    val ArrowLeft: ImageVector by lazy {
        material(
            name = "ArrowLeft",
            d = "m313 -440 224 224 -57 56 -320 -320 320 -320 57 56 -224 224h487v80H313Z",
        )
    }

    /** Google Material Symbols: `arrow_forward` (outline, 24 px). */
    val ArrowRight: ImageVector by lazy {
        material(
            name = "ArrowRight",
            d = "M647 -440H160v-80h487L423 -744l57 -56 320 320 -320 320 -57 -56 224 -224Z",
        )
    }

    /** Google Material Symbols: `arrow_upward` (outline, 24 px). */
    val ArrowUp: ImageVector by lazy {
        material(
            name = "ArrowUp",
            d = "M440 -160v-487L216 -423l-56 -57 320 -320 320 320 -56 57 -224 -224v487h-80Z",
        )
    }

    /** Google Material Symbols: `battery_full` (outline, 24 px). */
    val Battery: ImageVector by lazy {
        material(
            name = "Battery",
            d = "M320 -80q-17 0 -28.5 -11.5T280 -120v-640q0 -17 11.5 -28.5T320 -800h80v-80h160v80h80q17 0 28.5 11.5T680 -760v640q0 17 -11.5 28.5T640 -80H320Z",
        )
    }

    /** Google Material Symbols: `notifications` (outline, 24 px). */
    val Bell: ImageVector by lazy {
        material(
            name = "Bell",
            d = "M160 -200v-80h80v-280q0 -83 50 -147.5T420 -792v-28q0 -25 17.5 -42.5T480 -880q25 0 42.5 17.5T540 -820v28q80 20 130 84.5T720 -560v280h80v80H160Zm320 -300Zm0 420q-33 0 -56.5 -23.5T400 -160h160q0 33 -23.5 56.5T480 -80ZM320 -280h320v-280q0 -66 -47 -113t-113 -47q-66 0 -113 47t-47 113v280Z",
        )
    }

    /** Google Material Symbols: `notifications_off` (outline, 24 px). */
    val BellOff: ImageVector by lazy {
        material(
            name = "BellOff",
            d = "M160 -200v-80h80v-280q0 -33 8.5 -65t25.5 -61l60 60q-7 16 -10.5 32.5T320 -560v280h248L56 -792l56 -56 736 736 -56 56 -146 -144H160Zm560 -154 -80 -80v-126q0 -66 -47 -113t-113 -47q-26 0 -50 8t-44 24l-58 -58q20 -16 43 -28t49 -18v-28q0 -25 17.5 -42.5T480 -880q25 0 42.5 17.5T540 -820v28q80 20 130 84.5T720 -560v206Zm-276 -50Zm36 324q-33 0 -56.5 -23.5T400 -160h160q0 33 -23.5 56.5T480 -80Zm33 -481Z",
        )
    }

    /** Google Material Symbols: `bluetooth` (outline, 24 px). */
    val Bluetooth: ImageVector by lazy {
        material(
            name = "Bluetooth",
            d = "M440 -80v-304L256 -200l-56 -56 224 -224 -224 -224 56 -56 184 184v-304h40l228 228 -172 172 172 172L480 -80h-40Zm80 -496 76 -76 -76 -74v150Zm0 342 76 -74 -76 -76v150Z",
        )
    }

    /** Google Material Symbols: `bookmark` (filled, 24 px). */
    val Bookmark: ImageVector by lazy {
        material(
            name = "Bookmark",
            d = "M200 -120v-640q0 -33 23.5 -56.5T280 -840h400q33 0 56.5 23.5T760 -760v640L480 -240 200 -120Z",
        )
    }

    /** Google Material Symbols: `photo_camera` (outline, 24 px). */
    val Camera: ImageVector by lazy {
        material(
            name = "Camera",
            d = "M480 -260q75 0 127.5 -52.5T660 -440q0 -75 -52.5 -127.5T480 -620q-75 0 -127.5 52.5T300 -440q0 75 52.5 127.5T480 -260Zm0 -80q-42 0 -71 -29t-29 -71q0 -42 29 -71t71 -29q42 0 71 29t29 71q0 42 -29 71t-71 29ZM160 -120q-33 0 -56.5 -23.5T80 -200v-480q0 -33 23.5 -56.5T160 -760h126l74 -80h240l74 80h126q33 0 56.5 23.5T880 -680v480q0 33 -23.5 56.5T800 -120H160Zm0 -80h640v-480H638l-73 -80H395l-73 80H160v480Zm320 -240Z",
        )
    }

    /** Google Material Symbols: `chat` (outline, 24 px). */
    val Chat: ImageVector by lazy {
        material(
            name = "Chat",
            d = "M240 -400h320v-80H240v80Zm0 -120h480v-80H240v80Zm0 -120h480v-80H240v80ZM80 -80v-720q0 -33 23.5 -56.5T160 -880h640q33 0 56.5 23.5T880 -800v480q0 33 -23.5 56.5T800 -240H240L80 -80Zm126 -240h594v-480H160v525l46 -45Zm-46 0v-480 480Z",
        )
    }

    /** Google Material Symbols: `check` (outline, 24 px). */
    val Check: ImageVector by lazy {
        material(
            name = "Check",
            d = "M382 -240 154 -468l57 -57 171 171 367 -367 57 57 -424 424Z",
        )
    }

    /** Google Material Symbols: `content_paste` (outline, 24 px). */
    val Clipboard: ImageVector by lazy {
        material(
            name = "Clipboard",
            d = "M200 -120q-33 0 -56.5 -23.5T120 -200v-560q0 -33 23.5 -56.5T200 -840h167q11 -35 43 -57.5t70 -22.5q40 0 71.5 22.5T594 -840h166q33 0 56.5 23.5T840 -760v560q0 33 -23.5 56.5T760 -120H200Zm0 -80h560v-560h-80v120H280v-120h-80v560Zm308.5 -571.5Q520 -783 520 -800t-11.5 -28.5Q497 -840 480 -840t-28.5 11.5Q440 -817 440 -800t11.5 28.5Q463 -760 480 -760t28.5 -11.5Z",
        )
    }

    /** Google Material Symbols: `schedule` (outline, 24 px). */
    val Clock: ImageVector by lazy {
        material(
            name = "Clock",
            d = "m612 -292 56 -56 -148 -148v-184h-80v216l172 172ZM480 -80q-83 0 -156 -31.5T197 -197q-54 -54 -85.5 -127T80 -480q0 -83 31.5 -156T197 -763q54 -54 127 -85.5T480 -880q83 0 156 31.5T763 -763q54 54 85.5 127T880 -480q0 83 -31.5 156T763 -197q-54 54 -127 85.5T480 -80Zm0 -400Zm0 320q133 0 226.5 -93.5T800 -480q0 -133 -93.5 -226.5T480 -800q-133 0 -226.5 93.5T160 -480q0 133 93.5 226.5T480 -160Z",
        )
    }

    /** Google Material Symbols: `close` (outline, 24 px). */
    val Close: ImageVector by lazy {
        material(
            name = "Close",
            d = "m256 -200 -56 -56 224 -224 -224 -224 56 -56 224 224 224 -224 56 56 -224 224 224 224 -56 56 -224 -224 -224 224Z",
        )
    }

    /** Google Material Symbols: `code` (outline, 24 px). */
    val Code: ImageVector by lazy {
        material(
            name = "Code",
            d = "M320 -240 80 -480l240 -240 57 57 -184 184 183 183 -56 56Zm320 0 -57 -57 184 -184 -183 -183 56 -56 240 240 -240 240Z",
        )
    }

    /** Google Material Symbols: `storage` (outline, 24 px). */
    val Database: ImageVector by lazy {
        material(
            name = "Database",
            d = "M120 -160v-160h720v160H120Zm80 -40h80v-80h-80v80Zm-80 -440v-160h720v160H120Zm80 -40h80v-80h-80v80Zm-80 280v-160h720v160H120Zm80 -40h80v-80h-80v80Z",
        )
    }

    /** Google Material Symbols: `download` (outline, 24 px). */
    val Download: ImageVector by lazy {
        material(
            name = "Download",
            d = "M480 -320 280 -520l56 -58 104 104v-326h80v326l104 -104 56 58 -200 200ZM240 -160q-33 0 -56.5 -23.5T160 -240v-120h80v120h480v-120h80v120q0 33 -23.5 56.5T720 -160H240Z",
        )
    }

    /** Google Material Symbols: `edit` (outline, 24 px). */
    val Edit: ImageVector by lazy {
        material(
            name = "Edit",
            d = "M200 -200h57l391 -391 -57 -57 -391 391v57Zm-80 80v-170l528 -527q12 -11 26.5 -17t30.5 -6q16 0 31 6t26 18l55 56q12 11 17.5 26t5.5 30q0 16 -5.5 30.5T817 -647L290 -120H120Zm640 -584 -56 -56 56 56Zm-141 85 -28 -29 57 57 -29 -28Z",
        )
    }

    /** Google Material Symbols: `visibility` (outline, 24 px). */
    val Eye: ImageVector by lazy {
        material(
            name = "Eye",
            d = "M607.5 -372.5Q660 -425 660 -500t-52.5 -127.5Q555 -680 480 -680t-127.5 52.5Q300 -575 300 -500t52.5 127.5Q405 -320 480 -320t127.5 -52.5Zm-204 -51Q372 -455 372 -500t31.5 -76.5Q435 -608 480 -608t76.5 31.5Q588 -545 588 -500t-31.5 76.5Q525 -392 480 -392t-76.5 -31.5ZM214 -281.5Q94 -363 40 -500q54 -137 174 -218.5T480 -800q146 0 266 81.5T920 -500q-54 137 -174 218.5T480 -200q-146 0 -266 -81.5ZM480 -500Zm207.5 160.5Q782 -399 832 -500q-50 -101 -144.5 -160.5T480 -720q-113 0 -207.5 59.5T128 -500q50 101 144.5 160.5T480 -280q113 0 207.5 -59.5Z",
        )
    }

    /** Google Material Symbols: `description` (outline, 24 px). */
    val File: ImageVector by lazy {
        material(
            name = "File",
            d = "M320 -240h320v-80H320v80Zm0 -160h320v-80H320v80ZM240 -80q-33 0 -56.5 -23.5T160 -160v-640q0 -33 23.5 -56.5T240 -880h320l240 240v480q0 33 -23.5 56.5T720 -80H240Zm280 -520v-200H240v640h480v-440H520ZM240 -800v200 -200 640 -640Z",
        )
    }

    /** Google Material Symbols: `folder` (outline, 24 px). */
    val Folder: ImageVector by lazy {
        material(
            name = "Folder",
            d = "M160 -160q-33 0 -56.5 -23.5T80 -240v-480q0 -33 23.5 -56.5T160 -800h240l80 80h320q33 0 56.5 23.5T880 -640v400q0 33 -23.5 56.5T800 -160H160Zm0 -80h640v-400H447l-80 -80H160v480Zm0 0v-480 480Z",
        )
    }

    /** Google Material Symbols: `format_size` (outline, 24 px). */
    val FormatSize: ImageVector by lazy {
        material(
            name = "FormatSize",
            d = "M560 -160v-520H360v-120h520v120H680v520H560Zm-360 0v-320H80v-120h360v120H320v320H200Z",
        )
    }

    /** Google Material Symbols: `public` (outline, 24 px). */
    val Globe: ImageVector by lazy {
        material(
            name = "Globe",
            d = "M324 -111.5Q251 -143 197 -197t-85.5 -127Q80 -397 80 -480t31.5 -156Q143 -709 197 -763t127 -85.5Q397 -880 480 -880t156 31.5Q709 -817 763 -763t85.5 127Q880 -563 880 -480t-31.5 156Q817 -251 763 -197t-127 85.5Q563 -80 480 -80t-156 -31.5ZM440 -162v-78q-33 0 -56.5 -23.5T360 -320v-40L168 -552q-3 18 -5.5 36t-2.5 36q0 121 79.5 212T440 -162Zm276 -102q41 -45 62.5 -100.5T800 -480q0 -98 -54.5 -179T600 -776v16q0 33 -23.5 56.5T520 -680h-80v80q0 17 -11.5 28.5T400 -560h-80v80h240q17 0 28.5 11.5T600 -440v120h40q26 0 47 15.5t29 40.5Z",
        )
    }

    /** Google Material Symbols: `public` (filled, 24 px). */
    val GlobeFilled: ImageVector by lazy {
        material(
            name = "GlobeFilled",
            d = "M324 -111.5Q251 -143 197 -197t-85.5 -127Q80 -397 80 -480t31.5 -156Q143 -709 197 -763t127 -85.5Q397 -880 480 -880t156 31.5Q709 -817 763 -763t85.5 127Q880 -563 880 -480t-31.5 156Q817 -251 763 -197t-127 85.5Q563 -80 480 -80t-156 -31.5ZM440 -162v-78q-33 0 -56.5 -23.5T360 -320v-40L168 -552q-3 18 -5.5 36t-2.5 36q0 121 79.5 212T440 -162Zm276 -102q41 -45 62.5 -100.5T800 -480q0 -98 -54.5 -179T600 -776v16q0 33 -23.5 56.5T520 -680h-80v80q0 17 -11.5 28.5T400 -560h-80v80h240q17 0 28.5 11.5T600 -440v120h40q26 0 47 15.5t29 40.5Z",
        )
    }

    /** Google Material Symbols: `headphones` (outline, 24 px). */
    val Headphones: ImageVector by lazy {
        material(
            name = "Headphones",
            d = "M360 -120H200q-33 0 -56.5 -23.5T120 -200v-280q0 -75 28.5 -140.5t77 -114q48.5 -48.5 114 -77T480 -840q75 0 140.5 28.5t114 77q48.5 48.5 77 114T840 -480v280q0 33 -23.5 56.5T760 -120H600v-320h160v-40q0 -117 -81.5 -198.5T480 -760q-117 0 -198.5 81.5T200 -480v40h160v320Zm-80 -240h-80v160h80v-160Zm400 0v160h80v-160h-80Zm-400 0h-80 80Zm400 0h80 -80Z",
        )
    }

    /** Google Material Symbols: `favorite` (filled, 24 px). */
    val Heart: ImageVector by lazy {
        material(
            name = "Heart",
            d = "m480 -120 -58 -52q-101 -91 -167 -157T150 -447.5Q111 -500 95.5 -544T80 -634q0 -94 63 -157t157 -63q52 0 99 22t81 62q34 -40 81 -62t99 -22q94 0 157 63t63 157q0 46 -15.5 90T810 -447.5Q771 -395 705 -329T538 -172l-58 52Z",
        )
    }

    /** Google Material Symbols: `home` (filled, 24 px). */
    val Home: ImageVector by lazy {
        material(
            name = "Home",
            d = "M160 -120v-480l320 -240 320 240v480H560v-280H400v280H160Z",
        )
    }

    /** Google Material Symbols: `home` (outline, 24 px). */
    val HomeOutline: ImageVector by lazy {
        material(
            name = "HomeOutline",
            d = "M240 -200h120v-240h240v240h120v-360L480 -740 240 -560v360Zm-80 80v-480l320 -240 320 240v480H520v-240h-80v240H160Zm320 -350Z",
        )
    }

    /** Google Material Symbols: `image` (outline, 24 px). */
    val Image: ImageVector by lazy {
        material(
            name = "Image",
            d = "M200 -120q-33 0 -56.5 -23.5T120 -200v-560q0 -33 23.5 -56.5T200 -840h560q33 0 56.5 23.5T840 -760v560q0 33 -23.5 56.5T760 -120H200Zm0 -80h560v-560H200v560Zm40 -80h480L570 -480 450 -320l-90 -120 -120 160Zm-40 80v-560 560Z",
        )
    }

    /** Google Material Symbols: `info` (outline, 24 px). */
    val Info: ImageVector by lazy {
        material(
            name = "Info",
            d = "M440 -280h80v-240h-80v240Zm68.5 -331.5Q520 -623 520 -640t-11.5 -28.5Q497 -680 480 -680t-28.5 11.5Q440 -657 440 -640t11.5 28.5Q463 -600 480 -600t28.5 -11.5ZM480 -80q-83 0 -156 -31.5T197 -197q-54 -54 -85.5 -127T80 -480q0 -83 31.5 -156T197 -763q54 -54 127 -85.5T480 -880q83 0 156 31.5T763 -763q54 54 85.5 127T880 -480q0 83 -31.5 156T763 -197q-54 54 -127 85.5T480 -80Zm0 -80q134 0 227 -93t93 -227q0 -134 -93 -227t-227 -93q-134 0 -227 93t-93 227q0 134 93 227t227 93Zm0 -320Z",
        )
    }

    /** Google Material Symbols: `layers` (outline, 24 px). */
    val Layers: ImageVector by lazy {
        material(
            name = "Layers",
            d = "M480 -118 120 -398l66 -50 294 228 294 -228 66 50 -360 280Zm0 -202L120 -600l360 -280 360 280 -360 280Zm0 -280Zm0 178 230 -178 -230 -178 -230 178 230 178Z",
        )
    }

    /** Google Material Symbols: `link` (outline, 24 px). */
    val Link: ImageVector by lazy {
        material(
            name = "Link",
            d = "M440 -280H280q-83 0 -141.5 -58.5T80 -480q0 -83 58.5 -141.5T280 -680h160v80H280q-50 0 -85 35t-35 85q0 50 35 85t85 35h160v80ZM320 -440v-80h320v80H320Zm200 160v-80h160q50 0 85 -35t35 -85q0 -50 -35 -85t-85 -35H520v-80h160q83 0 141.5 58.5T880 -480q0 83 -58.5 141.5T680 -280H520Z",
        )
    }

    /** Google Material Symbols: `lock` (outline, 24 px). */
    val Lock: ImageVector by lazy {
        material(
            name = "Lock",
            d = "M240 -80q-33 0 -56.5 -23.5T160 -160v-400q0 -33 23.5 -56.5T240 -640h40v-80q0 -83 58.5 -141.5T480 -920q83 0 141.5 58.5T680 -720v80h40q33 0 56.5 23.5T800 -560v400q0 33 -23.5 56.5T720 -80H240Zm0 -80h480v-400H240v400Zm296.5 -143.5Q560 -327 560 -360t-23.5 -56.5Q513 -440 480 -440t-56.5 23.5Q400 -393 400 -360t23.5 56.5Q447 -280 480 -280t56.5 -23.5ZM360 -640h240v-80q0 -50 -35 -85t-85 -35q-50 0 -85 35t-35 85v80ZM240 -160v-400 400Z",
        )
    }

    /** Google Material Symbols: `mail` (outline, 24 px). */
    val Mail: ImageVector by lazy {
        material(
            name = "Mail",
            d = "M160 -160q-33 0 -56.5 -23.5T80 -240v-480q0 -33 23.5 -56.5T160 -800h640q33 0 56.5 23.5T880 -720v480q0 33 -23.5 56.5T800 -160H160Zm320 -280L160 -640v400h640v-400L480 -440Zm0 -80 320 -200H160l320 200ZM160 -640v-80 480 -400Z",
        )
    }

    /** Google Material Symbols: `location_on` (outline, 24 px). */
    val MapPin: ImageVector by lazy {
        material(
            name = "MapPin",
            d = "M536.5 -503.5Q560 -527 560 -560t-23.5 -56.5Q513 -640 480 -640t-56.5 23.5Q400 -593 400 -560t23.5 56.5Q447 -480 480 -480t56.5 -23.5ZM480 -186q122 -112 181 -203.5T720 -552q0 -109 -69.5 -178.5T480 -800q-101 0 -170.5 69.5T240 -552q0 71 59 162.5T480 -186Zm0 106Q319 -217 239.5 -334.5T160 -552q0 -150 96.5 -239T480 -880q127 0 223.5 89T800 -552q0 100 -79.5 217.5T480 -80Zm0 -480Z",
        )
    }

    /** Google Material Symbols: `menu` (outline, 24 px). */
    val Menu: ImageVector by lazy {
        material(
            name = "Menu",
            d = "M120 -240v-80h720v80H120Zm0 -200v-80h720v80H120Zm0 -200v-80h720v80H120Z",
        )
    }

    /** Google Material Symbols: `mic` (outline, 24 px). */
    val Mic: ImageVector by lazy {
        material(
            name = "Mic",
            d = "M395 -435q-35 -35 -35 -85v-240q0 -50 35 -85t85 -35q50 0 85 35t35 85v240q0 50 -35 85t-85 35q-50 0 -85 -35Zm85 -205Zm-40 520v-123q-104 -14 -172 -93t-68 -184h80q0 83 58.5 141.5T480 -320q83 0 141.5 -58.5T680 -520h80q0 105 -68 184t-172 93v123h-80Zm68.5 -371.5Q520 -503 520 -520v-240q0 -17 -11.5 -28.5T480 -800q-17 0 -28.5 11.5T440 -760v240q0 17 11.5 28.5T480 -480q17 0 28.5 -11.5Z",
        )
    }

    /** Google Material Symbols: `remove` (outline, 24 px). */
    val Minus: ImageVector by lazy {
        material(
            name = "Minus",
            d = "M200 -440v-80h560v80H200Z",
        )
    }

    /** Google Material Symbols: `monitor` (outline, 24 px). */
    val Monitor: ImageVector by lazy {
        material(
            name = "Monitor",
            d = "M240 -120v-80l40 -40H160q-33 0 -56.5 -23.5T80 -320v-440q0 -33 23.5 -56.5T160 -840h640q33 0 56.5 23.5T880 -760v440q0 33 -23.5 56.5T800 -240H680l40 40v80H240Zm-80 -200h640v-440H160v440Zm0 0v-440 440Z",
        )
    }

    /** Google Material Symbols: `dark_mode` (outline, 24 px). */
    val Moon: ImageVector by lazy {
        material(
            name = "Moon",
            d = "M480 -120q-150 0 -255 -105T120 -480q0 -150 105 -255t255 -105q14 0 27.5 1t26.5 3q-41 29 -65.5 75.5T444 -660q0 90 63 153t153 63q55 0 101 -24.5t75 -65.5q2 13 3 26.5t1 27.5q0 150 -105 255T480 -120Zm0 -80q88 0 158 -48.5T740 -375q-20 5 -40 8t-40 3q-123 0 -209.5 -86.5T364 -660q0 -20 3 -40t8 -40q-78 32 -126.5 102T200 -480q0 116 82 198t198 82Zm-10 -270Z",
        )
    }

    /** Google Material Symbols: `music_note` (outline, 24 px). */
    val Music: ImageVector by lazy {
        material(
            name = "Music",
            d = "M287 -167q-47 -47 -47 -113t47 -113q47 -47 113 -47 23 0 42.5 5.5T480 -418v-422h240v160H560v400q0 66 -47 113t-113 47q-66 0 -113 -47Z",
        )
    }

    /** Google Material Symbols: `pause` (outline, 24 px). */
    val Pause: ImageVector by lazy {
        material(
            name = "Pause",
            d = "M520 -200v-560h240v560H520Zm-320 0v-560h240v560H200Zm400 -80h80v-400h-80v400Zm-320 0h80v-400h-80v400Zm0 -400v400 -400Zm320 0v400 -400Z",
        )
    }

    /** Google Material Symbols: `call` (outline, 24 px). */
    val Phone: ImageVector by lazy {
        material(
            name = "Phone",
            d = "M798 -120q-125 0 -247 -54.5T329 -329Q229 -429 174.5 -551T120 -798q0 -18 12 -30t30 -12h162q14 0 25 9.5t13 22.5l26 140q2 16 -1 27t-11 19l-97 98q20 37 47.5 71.5T387 -386q31 31 65 57.5t72 48.5l94 -94q9 -9 23.5 -13.5T670 -390l138 28q14 4 23 14.5t9 23.5v162q0 18 -12 30t-30 12ZM241 -600l66 -66 -17 -94h-89q5 41 14 81t26 79Zm358 358q39 17 79.5 27t81.5 13v-88l-94 -19 -67 67ZM241 -600Zm358 358Z",
        )
    }

    /** Google Material Symbols: `play_arrow` (outline, 24 px). */
    val Play: ImageVector by lazy {
        material(
            name = "Play",
            d = "M320 -200v-560l440 280 -440 280Zm80 -280Zm0 134 210 -134 -210 -134v268Z",
        )
    }

    /** Google Material Symbols: `add` (outline, 24 px). */
    val Plus: ImageVector by lazy {
        material(
            name = "Plus",
            d = "M440 -440H200v-80h240v-240h80v240h240v80H520v240h-80v-240Z",
        )
    }

    /** Google Material Symbols: `refresh` (outline, 24 px). */
    val Refresh: ImageVector by lazy {
        material(
            name = "Refresh",
            d = "M480 -160q-134 0 -227 -93t-93 -227q0 -134 93 -227t227 -93q69 0 132 28.5T720 -690v-110h80v280H520v-80h168q-32 -56 -87.5 -88T480 -720q-100 0 -170 70t-70 170q0 100 70 170t170 70q77 0 139 -44t87 -116h84q-28 106 -114 173t-196 67Z",
        )
    }

    /** Google Material Symbols: `search` (outline, 24 px). */
    val Search: ImageVector by lazy {
        material(
            name = "Search",
            d = "M784 -120 532 -372q-30 24 -69 38t-83 14q-109 0 -184.5 -75.5T120 -580q0 -109 75.5 -184.5T380 -840q109 0 184.5 75.5T640 -580q0 44 -14 83t-38 69l252 252 -56 56ZM380 -400q75 0 127.5 -52.5T560 -580q0 -75 -52.5 -127.5T380 -760q-75 0 -127.5 52.5T200 -580q0 75 52.5 127.5T380 -400Z",
        )
    }

    /** Google Material Symbols: `send` (outline, 24 px). */
    val Send: ImageVector by lazy {
        material(
            name = "Send",
            d = "M120 -160v-640l760 320 -760 320Zm80 -120 474 -200 -474 -200v140l240 60 -240 60v140Zm0 0v-400 400Z",
        )
    }

    /** Google Material Symbols: `settings` (filled, 24 px). */
    val Settings: ImageVector by lazy {
        material(
            name = "Settings",
            d = "m370 -80 -16 -128q-13 -5-24.5 -12T307 -235l-119 50L78 -375l103 -78q-1 -7-1 -13.5v-27q0 -6.5 1 -13.5L78 -585l110 -190 119 50q11 -8 23 -15t24 -12l16 -128h220l16 128q13 5 24.5 12t22.5 15l119 -50 110 190 -103 78q1 7 1 13.5v27q0 6.5 -2 13.5l103 78 -110 190 -118 -50q-11 8 -23 15t-24 12L590 -80H370Zm112 -260q58 0 99 -41t41 -99q0 -58 -41 -99t-99 -41q-59 0 -99.5 41T342 -480q0 58 40.5 99t99.5 41Z",
        )
    }

    /** Google Material Symbols: `settings` (outline, 24 px). */
    val SettingsOutline: ImageVector by lazy {
        material(
            name = "SettingsOutline",
            d = "m370 -80 -16 -128q-13 -5-24.5 -12T307 -235l-119 50L78 -375l103 -78q-1 -7-1 -13.5v-27q0 -6.5 1 -13.5L78 -585l110 -190 119 50q11 -8 23 -15t24 -12l16 -128h220l16 128q13 5 24.5 12t22.5 15l119 -50 110 190 -103 78q1 7 1 13.5v27q0 6.5 -2 13.5l103 78 -110 190 -118 -50q-11 8 -23 15t-24 12L590 -80H370Zm70 -80h79l14 -106q31 -8 57.5 -23.5T639 -327l99 41 39 -68 -86 -65q5 -14 7 -29.5t2 -31.5q0 -16 -2-31.5t-7 -29.5l86 -65 -39 -68 -99 42q-22 -23 -48.5 -38.5T533 -694l-13 -106h-79l-14 106q-31 8 -57.5 23.5T321 -633l-99 -41 -39 68 86 64q-5 15 -7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99 -42q22 23 48.5 38.5T427 -266l13 106Zm42 -180q58 0 99 -41t41 -99q0 -58 -41 -99t-99 -41q-59 0 -99.5 41T342 -480q0 58 40.5 99t99.5 41Zm-2 -140Z",
        )
    }

    /** Google Material Symbols: `share` (outline, 24 px). */
    val Share: ImageVector by lazy {
        material(
            name = "Share",
            d = "M680 -80q-50 0 -85 -35t-35 -85q0 -6 3 -28L282 -392q-16 15 -37 23.5t-45 8.5q-50 0 -85 -35t-35 -85q0 -50 35 -85t85 -35q24 0 45 8.5t37 23.5l281 -164q-2 -7-2.5 -13.5T560 -760q0 -50 35 -85t85 -35q50 0 85 35t35 85q0 50 -35 85t-85 35q-24 0 -45 -8.5T598 -672L317 -508q2 7 2.5 13.5t.5 14.5q0 8-.5 14.5T317 -452l281 164q16 -15 37 -23.5t45 -8.5q50 0 85 35t35 85q0 50 -35 85t-85 35Zm0 -80q17 0 28.5 -11.5T720 -200q0 -17 -11.5 -28.5T680 -240q-17 0 -28.5 11.5T640 -200q0 17 11.5 28.5T680 -160ZM200 -440q17 0 28.5 -11.5T240 -480q0 -17 -11.5 -28.5T200 -520q-17 0 -28.5 11.5T160 -480q0 17 11.5 28.5T200 -440Zm508.5 -291.5Q720 -743 720 -760t-11.5 -28.5Q697 -800 680 -800t-28.5 11.5Q640 -777 640 -760t11.5 28.5Q663 -720 680 -720t28.5 -11.5ZM680 -200ZM200 -480Zm480 -280Z",
        )
    }

    /** Google Material Symbols: `smartphone` (outline, 24 px). */
    val Smartphone: ImageVector by lazy {
        material(
            name = "Smartphone",
            d = "M280 -40q-33 0 -56.5 -23.5T200 -120v-720q0 -33 23.5 -56.5T280 -920h400q33 0 56.5 23.5T760 -840v124q18 7 29 22t11 34v80q0 19 -11 34t-29 22v404q0 33 -23.5 56.5T680 -40H280Zm0 -80h400v-720H280v720Zm0 0v-720 720Zm228.5 -611.5Q520 -743 520 -760t-11.5 -28.5Q497 -800 480 -800t-28.5 11.5Q440 -777 440 -760t11.5 28.5Q463 -720 480 -720t28.5 -11.5Z",
        )
    }

    /** Google Material Symbols: `light_mode` (outline, 24 px). */
    val Sun: ImageVector by lazy {
        material(
            name = "Sun",
            d = "M565 -395q35 -35 35 -85t-35 -85q-35 -35 -85 -35t-85 35q-35 35 -35 85t35 85q35 35 85 35t85 -35Zm-226.5 56.5Q280 -397 280 -480t58.5 -141.5Q397 -680 480 -680t141.5 58.5Q680 -563 680 -480t-58.5 141.5Q563 -280 480 -280t-141.5 -58.5ZM200 -440H40v-80h160v80Zm720 0H760v-80h160v80ZM440 -760v-160h80v160h-80Zm0 720v-160h80v160h-80ZM256 -650l-101 -97 57 -59 96 100 -52 56Zm492 496 -97 -101 53 -55 101 97 -57 59Zm-98 -550 97 -101 59 57 -100 96 -56 -52ZM154 -212l101 -97 55 53 -97 101 -59 -57Zm326 -268Z",
        )
    }

    /** Google Material Symbols: `sell` (outline, 24 px). */
    val Tag: ImageVector by lazy {
        material(
            name = "Tag",
            d = "M856 -390 570 -104q-12 12 -27 18t-30 6q-15 0 -30 -6t-27 -18L103 -457q-11 -11 -17 -25.5T80 -513v-287q0 -33 23.5 -56.5T160 -880h287q16 0 31 6.5t26 17.5l352 353q12 12 17.5 27t5.5 30q0 15 -5.5 29.5T856 -390ZM513 -160l286 -286 -353 -354H160v286l353 354ZM260 -640q25 0 42.5 -17.5T320 -700q0 -25 -17.5 -42.5T260 -760q-25 0 -42.5 17.5T200 -700q0 25 17.5 42.5T260 -640Zm220 160Z",
        )
    }

    /** Google Material Symbols: `terminal` (outline, 24 px). */
    val Terminal: ImageVector by lazy {
        material(
            name = "Terminal",
            d = "M160 -160q-33 0 -56.5 -23.5T80 -240v-480q0 -33 23.5 -56.5T160 -800h640q33 0 56.5 23.5T880 -720v480q0 33 -23.5 56.5T800 -160H160Zm0 -80h640v-400H160v400Zm140 -40 -56 -56 103 -104 -104 -104 57 -56 160 160 -160 160Zm180 0v-80h240v80H480Z",
        )
    }

    /** Google Material Symbols: `delete` (outline, 24 px). */
    val Trash: ImageVector by lazy {
        material(
            name = "Trash",
            d = "M280 -120q-33 0 -56.5 -23.5T200 -200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33 -23.5 56.5T680 -120H280Zm400 -600H280v520h400v-520ZM360 -280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280 -720v520 -520Z",
        )
    }

    /** Google Material Symbols: `upload` (outline, 24 px). */
    val Upload: ImageVector by lazy {
        material(
            name = "Upload",
            d = "M440 -320v-326L336 -542l-56 -58 200 -200 200 200 -56 58 -104 -104v326h-80ZM240 -160q-33 0 -56.5 -23.5T160 -240v-120h80v120h480v-120h80v120q0 33 -23.5 56.5T720 -160H240Z",
        )
    }

    /** Google Material Symbols: `person` (outline, 24 px). */
    val User: ImageVector by lazy {
        material(
            name = "User",
            d = "M367 -527q-47 -47 -47 -113t47 -113q47 -47 113 -47t113 47q47 47 47 113t-47 113q-47 47 -113 47t-113 -47ZM160 -160v-112q0 -34 17.5 -62.5T224 -378q62 -31 126 -46.5T480 -440q66 0 130 15.5T736 -378q29 15 46.5 43.5T800 -272v112H160Zm80 -80h480v-32q0 -11 -5.5 -20T700 -306q-54 -27 -109 -40.5T480 -360q-56 0 -111 13.5T260 -306q-9 5 -14.5 14t-5.5 20v32Zm296.5 -343.5Q560 -607 560 -640t-23.5 -56.5Q513 -720 480 -720t-56.5 23.5Q400 -673 400 -640t23.5 56.5Q447 -560 480 -560t56.5 -23.5ZM480 -640Zm0 400Z",
        )
    }

    /** Google Material Symbols: `videocam` (outline, 24 px). */
    val Video: ImageVector by lazy {
        material(
            name = "Video",
            d = "M160 -160q-33 0 -56.5 -23.5T80 -240v-480q0 -33 23.5 -56.5T160 -800h480q33 0 56.5 23.5T720 -720v180l160 -160v440L720 -420v180q0 33 -23.5 56.5T640 -160H160Zm0 -80h480v-480H160v480Zm0 0v-480 480Z",
        )
    }

    /** Google Material Symbols: `volume_up` (outline, 24 px). */
    val Volume: ImageVector by lazy {
        material(
            name = "Volume",
            d = "M560 -131v-82q90 -26 145 -100t55 -168q0 -94 -55 -168T560 -749v-82q124 28 202 125.5T840 -481q0 127 -78 224.5T560 -131ZM120 -360v-240h160l200 -200v640L280 -360H120Zm440 40v-322q47 22 73.5 66t26.5 96q0 51 -26.5 94.5T560 -320ZM400 -606l-86 86H200v80h114l86 86v-252ZM300 -480Z",
        )
    }

    /** Google Material Symbols: `wifi` (outline, 24 px). */
    val Wifi: ImageVector by lazy {
        material(
            name = "Wifi",
            d = "M409 -149q-29 -29 -29 -71t29 -71q29 -29 71 -29t71 29q29 29 29 71t-29 71q-29 29 -71 29t-71 -29ZM254 -346l-84 -86q59 -59 138.5 -93.5T480 -560q92 0 171.5 35T790 -430l-84 84q-44 -44 -102 -69t-124 -25q-66 0 -124 25t-102 69ZM84 -516 0 -600q92 -94 215 -147t265 -53q142 0 265 53t215 147l-84 84q-77 -77 -178.5 -120.5T480 -680q-116 0 -217.5 43.5T84 -516Z",
        )
    }

    /** Google Material Symbols: `bolt` (outline, 24 px). */
    val Zap: ImageVector by lazy {
        material(
            name = "Zap",
            d = "m422 -232 207 -248H469l29 -227 -185 267h139l-30 208ZM320 -80l40 -280H160l360 -520h80l-40 320h240L400 -80h-80Zm151 -390Z",
        )
    }
}

/**
 * Собирает ImageVector из компонента одной иконки Google Material Symbols.
 * Google хранит иконки в координатах 0..960 по x и -960..0 по y, поэтому путь
 * обёрнут в группу со сдвигом по y на 960. Вырезы (отверстия) в иконках
 * получаются направлением обхода контуров, отдельная функция чётности не нужна.
 */
private fun material(
    name: String,
    d: String
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        addGroup(translationY = 960f)
        addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = SolidColor(Color.Black)
        )
        clearGroup()
    }.build()
