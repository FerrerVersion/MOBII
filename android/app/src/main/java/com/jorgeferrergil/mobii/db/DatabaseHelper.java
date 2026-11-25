package com.jorgeferrergil.mobii.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.jorgeferrergil.mobii.model.ParkingFavorito;
import com.jorgeferrergil.mobii.model.ParkingSpot;
import com.jorgeferrergil.mobii.model.Reserva;
import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DB_NAME = "mobii.db";
    private static final int DB_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        try {
            TableUtils.createTable(cs, ParkingSpot.class);
            TableUtils.createTable(cs, Reserva.class);
            TableUtils.createTable(cs, ParkingFavorito.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldV, int newV) {
        if (oldV < 2) {
            try { db.execSQL("ALTER TABLE reservas ADD COLUMN userId TEXT"); }
            catch (Exception ignored) {}
            try { TableUtils.createTableIfNotExists(cs, ParkingFavorito.class); }
            catch (SQLException e) { e.printStackTrace(); }
        }
        if (oldV < 3) {
            try { db.execSQL("ALTER TABLE reservas ADD COLUMN syncId INTEGER DEFAULT -1"); }
            catch (Exception ignored) {}
        }
    }

    public Dao<ParkingSpot, Integer> getParkingDao() throws SQLException {
        return getDao(ParkingSpot.class);
    }

    public Dao<Reserva, Integer> getReservaDao() throws SQLException {
        return getDao(Reserva.class);
    }

    public Dao<ParkingFavorito, Integer> getFavoritoDao() throws SQLException {
        return getDao(ParkingFavorito.class);
    }
}
