package ba.unsa.etf.rma.rma20siljakamina96.account;

import ba.unsa.etf.rma.rma20siljakamina96.data.Account;

public interface IAccountInteractor {
    public Account getAccount();
    public void modifyAccount(double budget, double totalLimit, double monthLimit);
}