package net.powermatcher.api;

import java.util.List;

public interface WhiteList {

    List<String> getWhiteList();
    
    List<String> createWhiteList(List<String> whiteList);
    
    List<String> addWhiteList(List<String> whiteList);
    
    List<String> removeWhiteList(List<String> whiteList);    
}
