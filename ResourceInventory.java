import java.util.*;

public class ResourceInventory {
    public enum ResourceType {
        OXYGEN, WATER, FOOD, POWER, MEDICAL, MAINTENANCE
    }
    
    private static class ResourceItem {
        public final ResourceType type;
        public double quantity;
        public double consumptionRate;
        public double minimumLevel;
        public String unit;
        
        public ResourceItem(ResourceType type, double initialQuantity, 
                          double consumptionRate, double minimumLevel, String unit) {
            this.type = type;
            this.quantity = initialQuantity;
            this.consumptionRate = consumptionRate;
            this.minimumLevel = minimumLevel;
            this.unit = unit;
        }
    }
    
    private final Map<ResourceType, ResourceItem> resources = new HashMap<>();
    private final List<String> inventoryLog = new ArrayList<>();
    
    public ResourceInventory() {
        initializeDefaultResources();
    }
    
    private void initializeDefaultResources() {
        resources.put(ResourceType.OXYGEN, new ResourceItem(ResourceType.OXYGEN, 5000, 50, 1000, "cubic meters"));
        resources.put(ResourceType.WATER, new ResourceItem(ResourceType.WATER, 10000, 200, 2000, "liters"));
        resources.put(ResourceType.FOOD, new ResourceItem(ResourceType.FOOD, 5000, 10, 500, "kg"));
        resources.put(ResourceType.POWER, new ResourceItem(ResourceType.POWER, 10000, 300, 2000, "kWh"));
        resources.put(ResourceType.MEDICAL, new ResourceItem(ResourceType.MEDICAL, 1000, 2, 100, "units"));
        resources.put(ResourceType.MAINTENANCE, new ResourceItem(ResourceType.MAINTENANCE, 500, 5, 50, "units"));
    }
    
    public boolean consumeResource(ResourceType type, double amount) {
        ResourceItem item = resources.get(type);
        if (item != null && item.quantity >= amount) {
            item.quantity -= amount;
            logConsumption(type, amount, item.quantity);
            return true;
        }
        return false;
    }
    
    public void addResource(ResourceType type, double amount) {
        ResourceItem item = resources.get(type);
        if (item != null) {
            item.quantity += amount;
            logAddition(type, amount, item.quantity);
        }
    }
    
    public double getDaysRemaining(ResourceType type) {
        ResourceItem item = resources.get(type);
        if (item == null || item.consumptionRate <= 0) return Double.POSITIVE_INFINITY;
        return item.quantity / item.consumptionRate;
    }
    
    public List<ResourceType> getLowResources() {
        List<ResourceType> lowResources = new ArrayList<>();
        for (ResourceItem item : resources.values()) {
            if (item.quantity <= item.minimumLevel) {
                lowResources.add(item.type);
            }
        }
        return lowResources;
    }
    
    public Map<ResourceType, Map<String, Object>> getResourceReport() {
        Map<ResourceType, Map<String, Object>> report = new HashMap<>();
        for (ResourceItem item : resources.values()) {
            Map<String, Object> resourceData = new HashMap<>();
            resourceData.put("quantity", item.quantity);
            resourceData.put("unit", item.unit);
            resourceData.put("consumptionRate", item.consumptionRate);
            resourceData.put("minimumLevel", item.minimumLevel);
            resourceData.put("daysRemaining", getDaysRemaining(item.type));
            resourceData.put("isLow", item.quantity <= item.minimumLevel);
            report.put(item.type, resourceData);
        }
        return report;
    }
    
    private void logConsumption(ResourceType type, double amount, double remaining) {
        String log = String.format("CONSUMED: %.2f %s of %s. Remaining: %.2f",
            amount, resources.get(type).unit, type, remaining);
        inventoryLog.add(log);
    }
    
    private void logAddition(ResourceType type, double amount, double total) {
        String log = String.format("ADDED: %.2f %s of %s. Total: %.2f",
            amount, resources.get(type).unit, type, total);
        inventoryLog.add(log);
    }
    
    public List<String> getInventoryLog() {
        return new ArrayList<>(inventoryLog);
    }
}