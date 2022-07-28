package fr.phoenix.contracts.gui;

import fr.phoenix.contracts.contract.ContractState;

/**
 * The state viewed (waiting acceptance/ open/dispute/ended)
 */
public enum ViewState {
    WAITING_ACCEPTANCE(ContractState.WAITING_ACCEPTANCE),
    OPEN(ContractState.OPEN),
    DISPUTED(ContractState.ADMIN_DISPUTED, ContractState.MIDDLEMAN_DISPUTED),
    ENDED(ContractState.RESOLVED, ContractState.FULFILLED);

    public final ContractState[] corresponding;

    ViewState(ContractState... corresponding) {
        this.corresponding = corresponding;
    }
}