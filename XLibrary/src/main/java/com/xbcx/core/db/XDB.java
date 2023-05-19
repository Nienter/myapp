package com.xbcx.core.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xbcx.core.IDObject;
import com.xbcx.core.XApplication;
import com.xbcx.im.db.IMDatabaseManager;
import com.xbcx.library.BuildConfig;
import com.xbcx.utils.SystemUtils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.text.TextUtils;

public class XDB{
	
	public static XDB getInstance(){
		if(sInstance == null){
			sInstance = new XDB();
		}
		return sInstance;
	}
	
	private static XDB sInstance;
	
	private XDB(){
	}
	
	public <T> List<T> readAll(Class<T> clazz,boolean bPrivate){
		return readAll(getTableName(clazz), bPrivate);
	}
	
	public <T> List<T> readAll(String tableName,boolean bPrivate){
		return readAll(tableName, null, bPrivate);
	}
	
	public <T> List<T> readAll(String tableName,String where,boolean bPrivate){
		boolean bNeedDelete = false;
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, null, where,
						null, null, null, null);
				dh.manageCursor(c);
				return toItems(c);
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				deleteAll(tableName, bPrivate);
			}
		}
		
		return new ArrayList<T>();
	}
	
	public <T> List<T> readAllOrderByUpdateTime(String tableName,boolean bDesc,boolean bPrivate){
		boolean bNeedDelete = false;
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, null, null,
						null, null, null, "update_time " + (bDesc ? "desc" : "asc"));
				dh.manageCursor(c);
				return toItems(c);
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				deleteAll(tableName, bPrivate);
			}
		}
		return new ArrayList<T>();
	}
	
	public <T> List<T> readAllIdLike(Class<T> clazz,String like,boolean bPrivate){
		return readAllIdLike(getTableName(clazz), like, bPrivate);
	}
	
	public <T> List<T> readAllIdLike(String tableName,String like,boolean bPrivate){
		boolean bNeedDelete = false;
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				String sql = "select * from " + tableName + " where id like \"" + like + "\"";
				c = db.rawQuery(sql, null);
				dh.manageCursor(c);
				return toItems(c);
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				deleteAll(tableName, bPrivate);
			}
		}
		return new ArrayList<T>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> toItems(Cursor c) throws Exception{
		List<T> items = new ArrayList<T>();
		if(c != null && c.moveToFirst()){
			do{
				items.add((T)createObject(c));
			}while(c.moveToNext());
		}
		return items;
	}
	
	public <T extends IDObject> HashMap<String, T> readAllToMap(Class<T> clazz,boolean bPrivate){
		return readAllToMap(getTableName(clazz), bPrivate);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IDObject> HashMap<String, T> readAllToMap(String tableName,boolean bPrivate){
		boolean bNeedDelete = false;
		DbHelper dh = new DbHelper();
		HashMap<String, T> map = new HashMap<String, T>();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, null, null,
						null, null, null, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					do{
						T item = (T)createObject(c);
						map.put(item.getId(), item);
					}while(c.moveToNext());
				}
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				deleteAll(tableName, bPrivate);
			}
		}
		return map;
	}
	
	public <T extends IDObject> HashMap<String, T> readAllToMapIdLike(Class<T> clazz,String like,boolean bPrivate){
		return readAllToMapIdLike(getTableName(clazz), like, bPrivate);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IDObject> HashMap<String, T> readAllToMapIdLike(String tableName,String like,boolean bPrivate){
		boolean bNeedDelete = false;
		DbHelper dh = new DbHelper();
		HashMap<String, T> map = new HashMap<String, T>();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				String sql = "select * from " + tableName + " where id like \"" + like + "\"";
				c = db.rawQuery(sql, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					do{
						T item = (T)createObject(c);
						map.put(item.getId(), item);
					}while(c.moveToNext());
				}
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				deleteAll(tableName, bPrivate);
			}
		}

		return map;
	}
	
	public <T> List<T> readAllOrderById(Class<T> clazz,boolean bDesc,boolean bPrivate){
		return readAllOrderById(clazz, null,bDesc,bPrivate);
	}
	
	public <T> List<T> readAllOrderById(Class<T> clazz,String where,boolean bDesc,boolean bPrivate){
		return readAllOrderById(getTableName(clazz), 
				where, bDesc, bPrivate);
	}
	
	public <T> List<T> readAllOrderById(String tableName,String where,boolean bDesc,boolean bPrivate){
		boolean bNeedDelete = false;
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, null, where,
						null, null, null, "id " + (bDesc ? "desc" : "asc"));
				dh.manageCursor(c);
				return toItems(c);
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				deleteAll(tableName, bPrivate);
			}
		}
		return new ArrayList<T>();
	}
	
	public <T> T readById(String id,Class<T> clazz,boolean bPrivate){
		return readById(getTableName(clazz), id, bPrivate);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T readById(String tableName,String id,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			boolean bNeedDelete = false;
			try{
				c = db.query(tableName, null, "id='" + id + "'",
						null, null, null, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					Object o = createObject(c);
					return (T)o;
				}
			}catch(Exception e){
				logException(e);
				bNeedDelete = true;
			}finally{
				dh.release();
			}
			if(bNeedDelete){
				delete(tableName,id, bPrivate);
			}
		}
		return null;
	}
	
	public <T> T readFirst(Class<T> clazz,boolean bPrivate){
		return readFirst(getTableName(clazz), bPrivate);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T readFirst(String tableName,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, null, null,
						null, null, null, null,"0,1");
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					Object o = createObject(c);
					return (T)o;
				}
			}catch(Exception e){
				logException(e);
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	public Cursor readCursor(ReadConfig rc){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(rc.bPrivate, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(rc.tableName, 
						rc.columns, 
						rc.where,
						null, 
						null,
						null, 
						rc.orderby,
						null);
				if(c != null && c.moveToFirst()){
					return c;
				}
				if(c != null){
					c.close();
				}
			}catch(Exception e){
				logException(e);
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T createObject(Cursor c) throws Exception{
		return (T)SystemUtils.byteArrayToObject(c.getBlob(c.getColumnIndex("data")));
	}
	
	public void deleteWhere(String tableName,String where,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, false);
		if(db != null){
			try{
				db.delete(tableName, where, null);
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
	}
	
	public void delete(String id,Class<?> clz,boolean bPrivate){
		delete(getTableName(clz), id, bPrivate);
	}
	
	public void delete(String tableName,String id){
		delete(tableName, id, true);
	}
	
	public void delete(String tableName,String id,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, false);
		if(db != null){
			try{
				db.delete(tableName, "id='" + id + "'", null);
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
	}
	
	public void deleteAll(Class<?> cls,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, false);
		if(db != null){
			final String tableName = getTableName(cls);
			try{
				db.delete(tableName, null, null);
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
	}
	
	public void deleteAll(String tableName,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, false);
		if(db != null){
			try{
				db.delete(tableName, null, null);
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T readMaxItem(String tableName,String maxField,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			try{
				Cursor c = db.query(tableName, null, null, null, null, null, maxField + " desc");
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					Object o = createObject(c);
					return (T)o;
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	public String readMaxField(String tableName,String maxField,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			try{
				String sql = "select max(" + maxField + ") from " + tableName;
				Cursor c = db.rawQuery(sql, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					return c.getString(0);
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	public <T> T readItem(String tableName,String where,XDBObjectCreator<T> creator){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(true, true);
		if(db != null){
			try{
				Cursor c = db.query(tableName, null, where, null, null, null, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					return creator.createObject(c);
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	public <T> T readItem(String tableName,String where){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(true, true);
		if(db != null){
			try{
				Cursor c = db.query(tableName, null, where, null, null, null, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					return createObject(c);
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	public Map<String, String> readLastColumns(String tableName,String[] columns,String orderByColumn){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(true, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, columns,
						null,null, null, null, 
						orderByColumn + " DESC","0,1");
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					HashMap<String, String> values = new HashMap<String, String>();
					final int nCount = c.getColumnCount();
					for(int nIndex = 0;nIndex < nCount;++nIndex){
						values.put(c.getColumnName(nIndex),c.getString(nIndex));
					}
					return values;
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return Collections.emptyMap();
	}
	
	public <T> T readLast(String tableName,String orderByColumn,XDBObjectCreator<T> creator){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(true, true);
		if(db != null){
			Cursor c = null;
			try{
				c = db.query(tableName, null, null,
						null, null, null, orderByColumn + " DESC","0,1");
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					return creator.createObject(c);
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return null;
	}
	
	/**
	 * 从readPos向前读readCount个
	 * @param tableName
	 * @param readPos
	 * @param readCount
	 * @param where
	 * @param groupBy
	 * @param creator
	 * @return
	 */
	public <T> List<T> readLimitReverse(String tableName,int readPos,int readCount,String where,String orderBy,XDBObjectCreator<T> creator){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(true, true);
		if(db != null){
			try{
				int startPos = readPos - readCount + 1;
				if(startPos < 0){
					readCount = readPos + 1;
					startPos = 0;
				}
				Cursor cursor = null;
				cursor = db.query(tableName, null, 
								where, null, null, null,
								orderBy, 
								startPos + "," + readCount);
				dh.manageCursor(cursor);
				if(cursor != null && cursor.moveToFirst()){
					final List<T> items = new ArrayList<T>();
					do{
						items.add(creator.createObject(cursor));
					}while(cursor.moveToNext());
					return items;
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return Collections.emptyList();
	}
	
	public boolean updateOrInsert(IDObject item,boolean bPrivate){
		return updateOrInsert(getTableName(item), item, bPrivate);
	}
	
	public boolean updateOrInsert(String tableName,IDObject item){
		return updateOrInsert(tableName, item, true);
	}
	
	public boolean updateOrInsert(String tableName,IDObject item,boolean bPrivate){
		return updateOrInsert(tableName, item, true, bPrivate);
	}
	
	/**
	 * 
	 * @param tableName
	 * @param item
	 * @param bPrivate
	 * @return true代表Insert
	 */
	public boolean updateOrInsert(String tableName,IDObject item,boolean bUpdateTime,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, false);
		if(db != null){
			ContentValues cv = new ContentValues();
			try{
				if(hasExtField(item)){
					Class<?> type = null;
					XDBAnnotation a = null;
					for(Class<?> clazz = item.getClass();clazz != null;clazz = clazz.getSuperclass()){
						for(Field f : clazz.getDeclaredFields()){
							f.setAccessible(true);
							a = f.getAnnotation(XDBAnnotation.class);
							if(a != null && a.extField()){
								type = f.getType();
								String s = null;
								if(type.equals(String.class)){
									Object o = f.get(item);
									s = o == null ? null : o.toString();
								}else if(type.equals(int.class)){
									s = String.valueOf(f.getInt(item));
								}else if(type.equals(boolean.class)){
									boolean b = f.getBoolean(item);
									s = b ? "1" : "0";
								}else if(type.equals(long.class)){
									s = String.valueOf(f.getLong(item));
								}else if(type.equals(double.class)){
									s = String.valueOf(f.getDouble(item));
								}else if(type.equals(float.class)){
									s = String.valueOf(f.getFloat(item));
								}
								if(s == null){
									s = "";
								}
								cv.put(f.getName(), s);
							}
						}
					}
				}
				cv.put("data", SystemUtils.objectToByteArray(item));
				return _updateOrInsert(db, tableName, cv, bUpdateTime,item);
			}catch(Exception e){
				if(!DBUtils.tabbleIsExist(tableName, db)){
					db.execSQL(buildCreateTableSql(tableName, item));
					if(cv.size() > 0){
						cv.put("id", item.getId());
						db.insert(tableName, null, cv);
						return true;
					}
				}else{
					if(handleSqliteNoColumnException(db, item, cv,tableName, null, e)){
						_updateOrInsert(db, tableName, cv, bUpdateTime, item);
					}
				}
			}finally{
				dh.release();
			}
		}
		return false;
	}
	
	@SuppressLint("NewApi") 
	protected boolean handleSqliteNoColumnException(SQLiteDatabase db,
			IDObject item,
			ContentValues cv,
			String tableName,
			String lastColumnName,
			Exception e){
		String columnName = null;
		try{
			final String msg = e.getMessage();
			final String flag = "no such column";
			if(msg != null && msg.contains(flag)){
				int index = msg.indexOf(":", flag.length() + 1);
				columnName = msg.substring(flag.length() + 1,index).trim();
				if(!cv.containsKey(columnName)){
					int length = 0;
					String tempName = null;
					if(Build.VERSION.SDK_INT >= 11){
						for(String key : cv.keySet()){
							if(columnName.contains(key)){
								if(key.length() > length){
									tempName = key;
									length = key.length();
								}
							}
						}
					}
					if(TextUtils.isEmpty(tempName)){
						db.execSQL("drop table " + tableName);
						db.execSQL(buildCreateTableSql(tableName, item));
						return true;
					}else{
						columnName = tempName;
					}
				}
				if(columnName.equals(lastColumnName)){
					db.execSQL("drop table " + tableName);
					db.execSQL(buildCreateTableSql(tableName, item));
					return true;
				}else{
					db.execSQL("alter table " + tableName + " add " + columnName + " TEXT");
					return true;
				}
			}
			return false;
		}catch(Exception e1){
			return handleSqliteNoColumnException(db, item, cv,tableName,columnName,e1);
		}
	}
	
	private boolean _updateOrInsert(SQLiteDatabase db,String tableName,
			ContentValues cv,
			boolean bUpdateTime,
			IDObject item){
		if(bUpdateTime){
			cv.put("update_time", XApplication.getFixSystemTime());
		}
		int ret = db.update(tableName, cv, 
				"id='" + item.getId() + "'", null);
		if(ret <= 0){
			cv.put("id", item.getId());
			db.insert(tableName, null, cv);
			return true;
		}
		return false;
	}
	
	private String buildCreateTableSql(String tableName,IDObject ido){
		if(hasExtField(ido)){
			StringBuffer sb = new StringBuffer("CREATE TABLE ")
			.append(tableName).append(" (id TEXT PRIMARY KEY, ")
			.append("update_time INTEGER, ");
			XDBAnnotation a = null;
			for(Class<?> clazz = ido.getClass();clazz != null;clazz = clazz.getSuperclass()){
				for(Field f : clazz.getDeclaredFields()){
					a = f.getAnnotation(XDBAnnotation.class);
					if(a != null && a.extField()){
						sb.append(f.getName()).append(" TEXT, ");
					}
				}
			}
			sb.append("data BLOB);");
			return sb.toString();
		}else{
			return "CREATE TABLE " + tableName + " (" +
					"id" + " TEXT PRIMARY KEY, " +
					"update_time" + " INTEGER, " +
					"data" + " BLOB);";
		}
	}
	
	private boolean hasExtField(IDObject ido){
		XDBImplementation i = null;
		for(Class<?> clazz = ido.getClass();clazz != null;clazz = clazz.getSuperclass()){
			i = clazz.getAnnotation(XDBImplementation.class);
			if(i != null && i.extField()){
				return true;
			}
		}
		return false;
	}
	
	public List<String> readTableNamesLike(String like){
		return readTableNamesLike(like, true);
	}
	
	public List<String> readTableNamesLike(String like,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			try{
				Cursor cursor = db.rawQuery("select tbl_name from sqlite_master where tbl_name like '" + like +"'", 
						null);
				dh.manageCursor(cursor);
				List<String> tableNames = new ArrayList<String>();
				if(cursor != null && cursor.moveToFirst()){
					do{
						tableNames.add(cursor.getString(0));
					}while(cursor.moveToNext());
				}
				return tableNames;
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return Collections.emptyList();
	}
	
	public int readCount(Class<?> clazz,boolean bPrivate){
		return readCount(getTableName(clazz), bPrivate);
	}
	
	public int readCount(String tableName,boolean bPrivate){
		return readCount(tableName, null, bPrivate);
	}
	
	public int readCount(String tableName,String where,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			try{
				Cursor c = db.query(tableName, new String[]{"count(*)"}, where, null, null, null, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					return c.getInt(0);
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return 0;
	}
	
	public int readCountLike(Class<?> clazz,String like,boolean bPrivate){
		return readCountLike(getTableName(clazz), like, bPrivate);
	}
	
	public int readCountLike(String tableName,String like,boolean bPrivate){
		DbHelper dh = new DbHelper();
		SQLiteDatabase db = dh.initial(bPrivate, true);
		if(db != null){
			try{
				String sql = "select count(*) from " + tableName + " where id like \"" + like + "\"";
				Cursor c = db.rawQuery(sql, null);
				dh.manageCursor(c);
				if(c != null && c.moveToFirst()){
					return c.getInt(0);
				}
			}catch(Exception e){
			}finally{
				dh.release();
			}
		}
		return 0;
	}
	
	private void logException(Exception e){
		if(BuildConfig.DEBUG){
			if(e instanceof SQLiteException){
				e.printStackTrace();
			}else{
				e.printStackTrace();
			}
		}
	}
	
	protected static DatabaseManager getDatabaseManager(boolean bPrivate){
		return bPrivate ? IMDatabaseManager.getInstance() : PublicDatabaseManager.getInstance();
	}
	
	public static String getTableName(Object item){
		return getTableName(item.getClass());
	}
	
	public static String getTableName(Class<?> clazz) {
		return clazz.getName().replace('.', '_');
	}
	
	public static class ReadConfig{
		String		tableName;
		String 		where;
		String[] 	columns;
		String		orderby;
		String		limit;
		boolean		bPrivate = true;
		
		public ReadConfig(String tableName){
			this.tableName = tableName;
		}
		
		public ReadConfig(Class<?> clz){
			this.tableName = getTableName(clz);
		}
		
		public ReadConfig setWhere(String where){
			this.where = where;
			return this;
		}
		
		public ReadConfig setColumns(String[] columns){
			this.columns = columns;
			return this;
		}
		
		public ReadConfig setOrderby(String orderby){
			this.orderby = orderby;
			return this;
		}
		
		public ReadConfig setLimit(String limit){
			this.limit = limit;
			return this;
		}
		
		public ReadConfig setPrivate(boolean bPrivate){
			this.bPrivate = bPrivate;
			return this;
		}
	}
	
	private static class DbHelper{
		DatabaseManager	mDM;
		SQLiteDatabase	mSQDb;
		boolean			mRead;
		
		Cursor			mCursor;
		
		public SQLiteDatabase initial(boolean bPrivate,boolean bRead){
			DatabaseManager dm = getDatabaseManager(bPrivate);
			mDM = dm;
			mRead = bRead;
			if(dm != null){
				mSQDb = bRead ? dm.lockReadableDatabase() : dm.lockWritableDatabase();
				return mSQDb;
			}
			return null;
		}
		
		public void manageCursor(Cursor cursor){
			mCursor = cursor;
		}
		
		public void release(){
			if(mCursor != null){
				mCursor.close();
			}
			if(mDM != null){
				if(mRead){
					mDM.unlockReadableDatabase(mSQDb);
				}else{
					mDM.unlockWritableDatabase(mSQDb);
				}
			}
		}
	}
}
