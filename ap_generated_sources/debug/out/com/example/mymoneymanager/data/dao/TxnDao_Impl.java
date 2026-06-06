package com.example.mymoneymanager.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.mymoneymanager.data.entity.Txn;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TxnDao_Impl implements TxnDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Txn> __insertionAdapterOfTxn;

  private final EntityDeletionOrUpdateAdapter<Txn> __deletionAdapterOfTxn;

  private final EntityDeletionOrUpdateAdapter<Txn> __updateAdapterOfTxn;

  public TxnDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTxn = new EntityInsertionAdapter<Txn>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `transactions` (`id`,`dateMillis`,`type`,`amount`,`accountId`,`toAccountId`,`categoryId`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Txn entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.dateMillis);
        if (entity.type == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.type);
        }
        statement.bindDouble(4, entity.amount);
        statement.bindLong(5, entity.accountId);
        statement.bindLong(6, entity.toAccountId);
        statement.bindLong(7, entity.categoryId);
        if (entity.note == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.note);
        }
      }
    };
    this.__deletionAdapterOfTxn = new EntityDeletionOrUpdateAdapter<Txn>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `transactions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Txn entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfTxn = new EntityDeletionOrUpdateAdapter<Txn>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `transactions` SET `id` = ?,`dateMillis` = ?,`type` = ?,`amount` = ?,`accountId` = ?,`toAccountId` = ?,`categoryId` = ?,`note` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Txn entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.dateMillis);
        if (entity.type == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.type);
        }
        statement.bindDouble(4, entity.amount);
        statement.bindLong(5, entity.accountId);
        statement.bindLong(6, entity.toAccountId);
        statement.bindLong(7, entity.categoryId);
        if (entity.note == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.note);
        }
        statement.bindLong(9, entity.id);
      }
    };
  }

  @Override
  public long insert(final Txn t) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfTxn.insertAndReturnId(t);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Txn t) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfTxn.handle(t);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final Txn t) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfTxn.handle(t);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<Txn>> getInRange(final long start, final long end) {
    final String _sql = "SELECT * FROM transactions WHERE dateMillis BETWEEN ? AND ? ORDER BY dateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<List<Txn>>() {
      @Override
      @Nullable
      public List<Txn> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateMillis");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfToAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "toAccountId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<Txn> _result = new ArrayList<Txn>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Txn _item;
            _item = new Txn();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            _item.dateMillis = _cursor.getLong(_cursorIndexOfDateMillis);
            if (_cursor.isNull(_cursorIndexOfType)) {
              _item.type = null;
            } else {
              _item.type = _cursor.getString(_cursorIndexOfType);
            }
            _item.amount = _cursor.getDouble(_cursorIndexOfAmount);
            _item.accountId = _cursor.getLong(_cursorIndexOfAccountId);
            _item.toAccountId = _cursor.getLong(_cursorIndexOfToAccountId);
            _item.categoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _item.note = null;
            } else {
              _item.note = _cursor.getString(_cursorIndexOfNote);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<Txn> getInRangeSync(final long start, final long end) {
    final String _sql = "SELECT * FROM transactions WHERE dateMillis BETWEEN ? AND ? ORDER BY dateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, start);
    _argIndex = 2;
    _statement.bindLong(_argIndex, end);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateMillis");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
      final int _cursorIndexOfToAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "toAccountId");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final List<Txn> _result = new ArrayList<Txn>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Txn _item;
        _item = new Txn();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.dateMillis = _cursor.getLong(_cursorIndexOfDateMillis);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _item.type = null;
        } else {
          _item.type = _cursor.getString(_cursorIndexOfType);
        }
        _item.amount = _cursor.getDouble(_cursorIndexOfAmount);
        _item.accountId = _cursor.getLong(_cursorIndexOfAccountId);
        _item.toAccountId = _cursor.getLong(_cursorIndexOfToAccountId);
        _item.categoryId = _cursor.getLong(_cursorIndexOfCategoryId);
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _item.note = null;
        } else {
          _item.note = _cursor.getString(_cursorIndexOfNote);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<Txn>> getAll() {
    final String _sql = "SELECT * FROM transactions ORDER BY dateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<List<Txn>>() {
      @Override
      @Nullable
      public List<Txn> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateMillis");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfToAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "toAccountId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final List<Txn> _result = new ArrayList<Txn>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Txn _item;
            _item = new Txn();
            _item.id = _cursor.getLong(_cursorIndexOfId);
            _item.dateMillis = _cursor.getLong(_cursorIndexOfDateMillis);
            if (_cursor.isNull(_cursorIndexOfType)) {
              _item.type = null;
            } else {
              _item.type = _cursor.getString(_cursorIndexOfType);
            }
            _item.amount = _cursor.getDouble(_cursorIndexOfAmount);
            _item.accountId = _cursor.getLong(_cursorIndexOfAccountId);
            _item.toAccountId = _cursor.getLong(_cursorIndexOfToAccountId);
            _item.categoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _item.note = null;
            } else {
              _item.note = _cursor.getString(_cursorIndexOfNote);
            }
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Double> sumByType(final String type, final long s, final long e) {
    final String _sql = "SELECT SUM(amount) FROM transactions WHERE type = ? AND dateMillis BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, s);
    _argIndex = 3;
    _statement.bindLong(_argIndex, e);
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public double sumIncomeForAccount(final long accId) {
    final String _sql = "SELECT SUM(amount) FROM transactions WHERE accountId = ? AND type = 'INCOME'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, accId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final double _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getDouble(0);
      } else {
        _result = 0.0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public double sumExpenseForAccount(final long accId) {
    final String _sql = "SELECT SUM(amount) FROM transactions WHERE accountId = ? AND type = 'EXPENSE'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, accId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final double _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getDouble(0);
      } else {
        _result = 0.0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public double sumTransferOutForAccount(final long accId) {
    final String _sql = "SELECT SUM(amount) FROM transactions WHERE accountId = ? AND type = 'TRANSFER'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, accId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final double _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getDouble(0);
      } else {
        _result = 0.0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public double sumTransferInForAccount(final long accId) {
    final String _sql = "SELECT SUM(amount) FROM transactions WHERE toAccountId = ? AND type = 'TRANSFER'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, accId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final double _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getDouble(0);
      } else {
        _result = 0.0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Txn getByIdSync(final long id) {
    final String _sql = "SELECT * FROM transactions WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dateMillis");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
      final int _cursorIndexOfToAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "toAccountId");
      final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final Txn _result;
      if (_cursor.moveToFirst()) {
        _result = new Txn();
        _result.id = _cursor.getLong(_cursorIndexOfId);
        _result.dateMillis = _cursor.getLong(_cursorIndexOfDateMillis);
        if (_cursor.isNull(_cursorIndexOfType)) {
          _result.type = null;
        } else {
          _result.type = _cursor.getString(_cursorIndexOfType);
        }
        _result.amount = _cursor.getDouble(_cursorIndexOfAmount);
        _result.accountId = _cursor.getLong(_cursorIndexOfAccountId);
        _result.toAccountId = _cursor.getLong(_cursorIndexOfToAccountId);
        _result.categoryId = _cursor.getLong(_cursorIndexOfCategoryId);
        if (_cursor.isNull(_cursorIndexOfNote)) {
          _result.note = null;
        } else {
          _result.note = _cursor.getString(_cursorIndexOfNote);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
