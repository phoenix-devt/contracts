package fr.phoenix.contracts.manager;

import fr.phoenix.contracts.Contracts;
import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.player.PlayerData;
import fr.phoenix.contracts.utils.ConfigFile;

import java.util.*;

public class MiddlemenManager {
    private final List<UUID> middlemenList = new ArrayList<>();

    private final Deque<Contract> waitingForMiddleman = new ArrayDeque<>();

    public void load() {
        ConfigFile config = new ConfigFile("middlemen");
        for (String id : config.getConfig().getStringList("middlemen")) {
            middlemenList.add(UUID.fromString(id));
        }
        for (String contractId : config.getConfig().getStringList("waiting-middleman-contracts")) {
            waitingForMiddleman.add(Contracts.plugin.contractManager.get(UUID.fromString(contractId)));
        }
    }

    public void save() {
        ConfigFile configFile = new ConfigFile("middlemen");

        for (UUID uuid : middlemenList) {
            configFile.getConfig().set("middlemen", middlemenList
                    .stream()
                    .map(UUID::toString)
                    .toList());
            configFile.getConfig().set("waiting-middleman-contracts", waitingForMiddleman
                    .stream()
                    .map(contract -> contract.getId().toString())
                    .toList());
        }
        configFile.save();
    }

    public void removeMiddlemen(PlayerData playerData) {
        if (playerData.isMiddleman()) {
            middlemenList.remove(playerData.getUuid());
            //We reassign all the contracts of the middlemen.
            playerData.getMiddlemanContracts().values()
                    .stream()
                    .filter(contract -> contract.getState()== ContractState.MIDDLEMAN_DISPUTED)
                    .forEach(contract-> assignToRandomMiddleman(contract));
            playerData.getMiddlemanContracts().clear();
        }
    }


    public void assignToRandomMiddleman(Contract contract) {
        List<PlayerData> assignableMiddlemen = middlemenList
                .stream()
                .map(uuid -> PlayerData.get(uuid))
                .filter(playerData -> playerData != null && playerData.isMiddleman()
                        &&playerData.getNumberOpenedMiddlemanContracts()<Contracts.plugin.configManager.maxContractsPerMiddleman)
                .toList();
        //If there's no middleman to assign the contract to, puts it in a waiting queue.
        if(assignableMiddlemen.size()==0) {
            waitingForMiddleman.add(contract);
        }
        else {
            int random = (int)(Math.random() * assignableMiddlemen.size());
            assignableMiddlemen.get(random).assignMiddlemanContract(contract);
        }
    }

    public void assignWaitingContractsToMiddleman(PlayerData middleman) {
        int contractsToAssign=Contracts.plugin.configManager.maxContractsPerMiddleman- middleman.getNumberOpenedMiddlemanContracts();
        for(int i=0;i<contractsToAssign;i++) {
            if(waitingForMiddleman.isEmpty())
                return;
            middleman.assignMiddlemanContract(waitingForMiddleman.remove());
        }
    }


}
