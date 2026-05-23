package com.syfe.finance_manager.dto;

import java.util.List;

public class TransactionsListResponse {
    private List<TransactionResponse> transactions;

    public TransactionsListResponse() {}

    public TransactionsListResponse(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }

    public List<TransactionResponse> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }
}
