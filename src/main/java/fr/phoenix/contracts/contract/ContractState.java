package fr.phoenix.contracts.contract;

public enum ContractState {

    /**
     * Contract was created by the employer but
     * the employee hasn't accepted it yet.
     */
    WAITING_ACCEPTANCE,

    /**
     * Contract was created by the employer and the
     * employee is working on it
     */
    OPEN,

    /**
     * Contract has been completed by the employee
     */
    FULFILLED,

    /**
     * A middleman is reviewing his case
     */
    MIDDLEMAN_DISPUTED,

    /**
     * A middle man has given his decision but an appeal to admins can be made.
     */
    MIDDLEMAN_RESOLVED,
    /**
     *
     */
    ADMIN_DISPUTED,

    /**
     *
     */
    RESOLVED;
}
