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
     * Employee/employer has opened a dispute and is
     * waiting for a middleman to review his case
     */
    WAITING_MIDDLEMAN,

    /**
     * A middleman is reviewing his case
     */
    MIDDLEMAN_DISPUTED,

    /**
     *
     */
    ADMIN_DISPUTED,

    /**
     *
     */
    RESOLVED;
}
