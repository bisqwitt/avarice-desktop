package com.avaricious.app;

import com.avaricious.game.run.GameSession;
import com.avaricious.persistence.RunSaveManager;
import com.avaricious.persistence.rundata.RunDataFileManager;

/** Application-owned services created in the composition root. */
public final class AppServices {

    private final RunDataFileManager runDataFiles =
        new RunDataFileManager();
    private final GameSession gameSession = new GameSession(runDataFiles);
    private final RunSaveManager runSaves = new RunSaveManager(gameSession);

    public GameSession gameSession() {
        return gameSession;
    }

    public RunSaveManager runSaves() {
        return runSaves;
    }

    public RunDataFileManager runDataFiles() {
        return runDataFiles;
    }
}
