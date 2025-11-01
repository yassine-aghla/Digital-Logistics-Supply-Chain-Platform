package org.example.digitallogisticssupplychainplatform.mapper;



import org.example.digitallogisticssupplychainplatform.entity.Inventory;
import org.example.digitallogisticssupplychainplatform.dto.InventoryDTO;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryDTO toDto(Inventory inventory) {
        if (inventory == null) {
            return null;
        }

        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setQtyOnHand(inventory.getQtyOnHand());
        dto.setQtyReserved(inventory.getQtyReserved());

        if (inventory.getWarehouse() != null) {
            dto.setWarehouseId(inventory.getWarehouse().getId());
            dto.setWarehouseCode(inventory.getWarehouse().getCode());
            dto.setWarehouseName(inventory.getWarehouse().getName());
        }

        return dto;
    }

    public Inventory toEntity(InventoryDTO dto) {
        if (dto == null) {
            return null;
        }

        Inventory inventory = new Inventory();
        inventory.setId(dto.getId());
        inventory.setQtyOnHand(dto.getQtyOnHand());
        inventory.setQtyReserved(dto.getQtyReserved());

        return inventory;
    }
}