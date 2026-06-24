<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F5F5F5"
    android:padding="16dp">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <!-- Header: Verse Number & Favorite Button -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="16dp">

            <TextView
                android:id="@+id/verseNumber"
                android:textSize="20sp"
                android:textStyle="bold"
                android:textColor="#333333"
                android:layout_width="0dp"
                android:layout_weight="1"
                android:layout_height="wrap_content"/>

            <ImageButton
                android:id="@+id/favButton"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/btn_star_big_off"
                android:contentDescription="علاقه‌مندی" />
        </LinearLayout>

        <!-- Card 1: Arabic Text -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="12dp"
            app:cardElevation="4dp"
            android:layout_marginBottom="12dp">
            
            <TextView
                android:id="@+id/arabicText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="16dp"
                android:textSize="26sp"
                android:textColor="#000000"
                android:gravity="center"
                android:textDirection="rtl"/>
        </androidx.cardview.widget.CardView>

        <!-- Card 2: Translation -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="12dp"
            app:cardElevation="2dp"
            android:layout_marginBottom="12dp">
            
            <TextView
                android:id="@+id/translation"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="16dp"
                android:textSize="18sp"
                android:textColor="#444444"
                android:lineSpacingExtra="4dp"
                android:textDirection="rtl"/>
        </androidx.cardview.widget.CardView>

        <!-- Card 3: Practical Application -->
        <androidx.cardview.widget.CardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="12dp"
            app:cardElevation="2dp"
            app:cardBackgroundColor="#E3F2FD"
            android:layout_marginBottom="24dp">
            
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">
                
                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="💡 کاربرد در زندگی:"
                    android:textStyle="bold"
                    android:textColor="#1976D2"
                    android:layout_marginBottom="8dp"/>
                    
                <TextView
                    android:id="@+id/exampleText"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:textSize="16sp"
                    android:textColor="#333333"
                    android:lineSpacingExtra="4dp"
                    android:textDirection="rtl"/>
            </LinearLayout>
        </androidx.cardview.widget.CardView>

        <Button
            android:id="@+id/doneButton"
            android:text="✅ انجام دادم"
            android:textSize="18sp"
            android:padding="12dp"
            android:backgroundTint="#4CAF50"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>

        <Button
            android:id="@+id/nextButton"
            android:text="آیه بعدی ➔"
            style="?attr/materialButtonOutlinedStyle"
            android:layout_marginTop="8dp"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>

    </LinearLayout>
</ScrollView>
