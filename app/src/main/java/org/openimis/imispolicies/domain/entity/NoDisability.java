package org.openimis.imispolicies.domain.entity;

public class NoDisability {
    private int id;
    private String code;
    private String name;
    private String altLanguage;

    // Constructors
    public NoDisability() {}
    
    public NoDisability(int id, String code, String name, String altLanguage) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.altLanguage = altLanguage;
    }

    // Getters and Setters
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getCode() { 
        return code; 
    }
    
    public void setCode(String code) { 
        this.code = code; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getAltLanguage() { 
        return altLanguage; 
    }
    
    public void setAltLanguage(String altLanguage) { 
        this.altLanguage = altLanguage; 
    }
    
    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
