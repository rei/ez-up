package com.rei.ezup.index;

import java.io.IOException;
import java.util.List;

import org.eclipse.aether.repository.RemoteRepository;

public interface RemoteRepositoryIndexer {
    List<String> getIndexes(RemoteRepository repo) throws IOException;
}
