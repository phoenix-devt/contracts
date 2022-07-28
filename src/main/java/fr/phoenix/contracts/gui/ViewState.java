package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.contract.Contract;
import fr.phoenix.contracts.contract.ContractState;
import fr.phoenix.contracts.player.PlayerData;

import java.util.List;
import java.util.function.Function;

/**
 * The state viewed (waiting acceptance/ open/dispute/ended)
 */
public enum ViewState {
    WAITING_ACCEPTANCE(playerData -> playerData.getContracts(ContractState.WAITING_ACCEPTANCE)),
    OPEN(playerData -> playerData.getContracts(ContractState.OPEN)),
    DISPUTED(playerData -> playerData.getContracts(ContractState.DISPUTED)),
    ENDED(playerData -> playerData.getContracts(ContractState.RESOLVED, ContractState.FULFILLED));

    private final Function<PlayerData, List<Contract>> contractsProvider;

    ViewState(Function<PlayerData, List<Contract>> contractsProvider) {
        this.contractsProvider = contractsProvider;
    }

    public List<Contract> provide(PlayerData playerData) {
        return contractsProvider.apply(playerData);
    }

}