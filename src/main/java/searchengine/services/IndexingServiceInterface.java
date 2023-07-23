package searchengine.services;

import searchengine.model.Page;

import java.util.ArrayList;

public interface IndexingServiceInterface {
    public void addLemmaAndIndex(Page page);
    ArrayList<Page> compute();
}
