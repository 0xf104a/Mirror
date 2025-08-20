package io.f104a.mirror.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.service.quicksettings.TileService;

import io.f104a.mirror.MainActivity;

/**
 * Quick settings tile, intended to call app when on lock screen
 */
public class QSTileService extends TileService {
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required for starting activity from TileService
        intent.putExtra("launch_source", "FROM_TILE");

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE for security
        );

        // Start the activity using the PendingIntent
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }

        // Toggle tile state (optional, to show the tile is active or inactive)
        /*Tile tile = getQsTile();
        if (tile.getState() == Tile.STATE_ACTIVE) {
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            tile.setState(Tile.STATE_ACTIVE);
        }
        tile.updateTile();*/
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }
}
