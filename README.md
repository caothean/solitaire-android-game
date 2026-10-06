# 🎴 Solitaire - Professional Android Card Game

Một tựa game Solitaire chuẩn (Klondike) được phát triển bằng **Kotlin + Android Canvas** với hệ thống quảng cáo AdMob, lưu trữ dữ liệu, và các tính năng giữ chân người chơi.

## 🎮 Tính Năng Chính

### Gameplay
- ✅ Klondike Solitaire chuẩn với 5 mức độ khó (Easy, Normal, Hard, Extreme, Legendary)
- ✅ Kéo thả (Drag & Drop) mượt mà và Tap-to-move
- ✅ Undo, Hint, và các booster giúp đỡ
- ✅ Hiệu ứng hình ảnh (VFX) khi chia bài, xếp được dãy, và thắng cuộc
- ✅ Âm thanh (SFX) cho các hành động

### Retention Features
1. **Daily Quests** - 3 nhiệm vụ hàng ngày với phần thưởng điểm
2. **Card Collection** - Thu thập mảnh ghép để mở khóa bộ bài mới (10 mảnh = 1 bộ)
3. **Reward Wheel** - Quay số 1 lần/ngày để nhận phần thưởng
4. **Login Streak** - Bonus điểm khi đăng nhập liên tiếp
5. **Achievement System** - Khám phá và unlock các thành tích

### Monetization
- 📺 Banner Ads (Top of screen)
- 🎬 Interstitial Ads (Sau 20 ván chơi, cứ 2-3 ván 1 lần)
- 💾 Local leaderboard (Top wins)

### Data Persistence
- 📊 Room Database cho lịch sử thắng, quests, collection
- 💾 SharedPreferences cho game stats
- 🔄 Save/Load game mid-play

## 📋 Cấu Trúc Project

```
solitaire-android-game/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/solitaire/game/
│   │   │   ├── MainActivity.kt              # Main menu
│   │   │   ├── GameActivity.kt              # Gameplay screen
│   │   │   ├── managers/
│   │   │   │   ├── GameManager.kt           # Core game logic
│   │   │   │   ├── CardManager.kt
│   │   │   │   ├── AdManager.kt             # AdMob integration
│   │   │   │   ├── AudioManager.kt
│   │   │   │   ├── SaveManager.kt           # Database operations
│   │   │   │   ├── FeatureManager.kt        # Retention features
│   │   │   │   └── AssetManager.kt
│   │   │   ├── models/
│   │   │   │   ├── Card.kt
│   │   │   │   ├── Difficulty.kt
│   │   │   │   ├── GameState.kt
│   │   │   │   ├── WinHistory.kt
│   │   │   │   └── DailyQuest.kt
│   │   │   ├── database/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── WinHistoryDao.kt
│   │   │   │   ├── DailyQuestDao.kt
│   │   │   │   └── CardCollectionDao.kt
│   │   │   ├── ui/
│   │   │   │   ├── GameView.kt              # Canvas drawing
│   │   │   │   ├── HistoryFragment.kt
│   │   │   │   └── QuestPanelView.kt
│   │   │   ├── utils/
│   │   │   │   ├── Constants.kt
│   │   │   │   └── Extensions.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_game.xml
│   │   │   │   ├── fragment_history.xml
│   │   │   │   └── quest_item.xml
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   ├── styles.xml
│   │   │   │   └── dimens.xml
│   │   │   ├── drawable/          # Placeholder cho card images
│   │   │   └── raw/               # Placeholder cho audio files
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts                # Project-level
├── settings.gradle.kts
└── proguard-rules.pro
```

## 🚀 Hướng Dẫn Setup

### 1. Clone Repository
```bash
git clone https://github.com/caothean/solitaire-android-game.git
cd solitaire-android-game
```

### 2. Mở trong Android Studio
- File → Open → Chọn thư mục project
- Đợi Gradle sync

### 3. Thay thế AdMob Unit IDs
**File: `app/src/main/kotlin/com/solitaire/game/managers/AdManager.kt`**

```kotlin
companion object {
    // Thay bằng IDs từ AdMob Console
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"
}
```

**File: `AndroidManifest.xml`**

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-xxxxxxxxxxxxxxxx~zzzzzzzzzz" />
```

### 4. Thêm Card Images & Audio Files

**Card Images** (`res/drawable/`):
- Cần 52 file `.png` cho các thẻ (AH, 2H, 3H, ..., KS)
- 1 file `card_back.png` cho mặt sau
- 1 file `card_placeholder.png` cho placeholder
- Kích thước: 80x120 dp (hoặc scale tương ứng)

**Audio Files** (`res/raw/`):
- `card_flip.mp3` - Tiếng lật bài
- `card_place.mp3` - Tiếng đặt bài
- `win.mp3` - Tiếng thắng
- `error.mp3` - Tiếng lỗi
- `background_music.mp3` - Nhạc nền

### 5. Build & Run
```bash
./gradlew build
./gradlew installDebug  # Hoặc từ Android Studio: Run > Run 'app'
```

## 📊 Difficulty Levels

| Level | Cards Drawn | Max Reshuffles | Time Limit | Points x |
|-------|-------------|----------------|------------|----------|
| **Easy** | 1 | Unlimited | No | 1.0x |
| **Normal** | 1 | 3 | No | 1.2x |
| **Hard** | 3 | 3 | No | 1.5x |
| **Extreme** | 3 | 1 | 5 min | 2.0x |
| **Legendary** | 3 | 1 | 3 min | 3.0x |

## 💡 Boosters (Mỗi ván được 3 của mỗi loại)

- **Hint** (💡) - Gợi ý nước đi hợp lệ tiếp theo
- **Undo** (↶) - Hoàn tác lần di chuyển cuối
- **Reveal** (👁️) - Mở khóa 1 thẻ ẩn trong tableau

## 📱 AdMob Strategy

```
Total Games Played < 20  → Không có Interstitial Ads
                   >= 20 → Cứ 2-3 ván (random) hiển thị 1 lần
                          Duration: 5-15 giây
                          Có nút close (X)

Banner Ads: Luôn ở top screen
```

## 🎯 Daily Quests Examples

1. "Win 5 Easy games" → 50 points + 1 collection piece
2. "Win 1 Hard game under 3 minutes" → 100 points + 1 piece
3. "Make 100 moves total" → 75 points + 1 piece
4. "Win 3 games in a row" → 80 points
5. "Win without using Undo" → 120 points

Bonus: Đăng nhập liên tiếp → +10 points/ngày

## 🎨 Card Collection (Unlock Skins)

Cần 10 mảnh ghép để unlock 1 bộ bài:
- **Classic** (Mặc định - sẵn có)
- **Gold** (Vàng, sang trọng)
- **Neon** (Neon glow, hiện đại)
- **Nature** (Cảnh tự nhiên)
- **Vintage** (Cổ điển)

Mảnh được nhận từ: Daily Quests, Reward Wheel, Achievements

## 🔧 Troubleshooting

### Lỗi: "Failed to resolve com.google.android.gms:play-services-ads"
- Kiểm tra `build.gradle` có đúng version
- Chạy: `./gradlew clean build --refresh-dependencies`

### Lỗi: "Database schema does not match"
- Xóa app data: Adb shell pm clear com.solitaire.game
- Hoặc tăng database version trong `AppDatabase.kt`

### Ads không hiển thị
- Dùng test Ad Unit IDs từ Google (ở AdManager.kt)
- Sau khi release, thay bằng production IDs

## 📈 Metrics to Track

- Total games played
- Win rate %
- Average completion time
- Daily active users (DAU)
- Ad impressions & CTR
- Daily quest completion %

## 🎮 Testing Checklist

- [ ] Tất cả 5 difficulty level khả dụng
- [ ] Drag & drop hoạt động mượt
- [ ] Undo/Hint/Booster hoạt động
- [ ] Win condition kích hoạt đúng
- [ ] Database lưu lịch sử correctly
- [ ] Daily quests reset hàng ngày
- [ ] Collection pieces tích lũy
- [ ] Banner ad load
- [ ] Interstitial ad show sau 20 ván
- [ ] Audio play khi enabled
- [ ] VFX animation smooth

## 📄 License

MIT License - Feel free to modify and use

## 👨‍💻 Author

Developed as a comprehensive Android game development example

---

**Happy Gaming! 🎴✨**
