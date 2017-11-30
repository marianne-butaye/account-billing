package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

  public void cancelInvoiceAndRedistributeFunds(BillId billId) {
    Bill currentBill = BillDAO.getInstance().findBill(billId);
    if (!(currentBill == null)) {
      ClientId clientId = currentBill.getClientId();

      if (currentBill.isCancelled() != true) {
        currentBill.cancel();
      }
      BillDAO.getInstance().persist(currentBill);

      List<Allocation> allocations = currentBill.getAllocations();

      for (Allocation allocation : allocations) {
        List<Bill> bills = BillDAO.getInstance().findAllByClient(clientId);
        int amount = allocation.getAmount();

        for (Bill bill : bills) {
          if (currentBill != bill) {
            int remainingAmount = bill.getRemainingAmount();
            Allocation newAllocation;
            if (remainingAmount <= amount) {
              newAllocation = new Allocation(remainingAmount);
              amount -= remainingAmount;
            } else {
              newAllocation = new Allocation(amount);
              amount = 0;
            }

            bill.addAllocation(newAllocation);

            BillDAO.getInstance().persist(bill);
          }

          if (amount == 0) {
            break;
          }
        }
      }
    } else {
      throw new BillNotFoundException();
    }
  }
}
