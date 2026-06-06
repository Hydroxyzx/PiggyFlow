package com.example.mymoneymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mymoneymanager.data.entity.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Insert long insert(Category c);
    @Update void update(Category c);
    @Delete void delete(Category c);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    LiveData<List<Category>> getByType(String type);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    List<Category> getByTypeSync(String type);

    @Query("SELECT * FROM categories ORDER BY type, name")
    LiveData<List<Category>> getAll();

    @Query("SELECT COUNT(*) FROM categories")
    int count();
}
