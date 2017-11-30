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

      List<Allocation> currentBillAllocations = currentBill.getAllocations();

      for (Allocation currentBillAllocation : currentBillAllocations) {
        List<Bill> clientBills = BillDAO.getInstance().findAllByClient(clientId);
        int amountLeftToRedistribute = currentBillAllocation.getAmount();

        for (Bill clientBill : clientBills) {
          if (currentBill != clientBill) {
            int remainingAmountOnClientBill = clientBill.getRemainingAmount();
            Allocation redistributedAllocation;
            if (remainingAmountOnClientBill <= amountLeftToRedistribute) {
              redistributedAllocation = new Allocation(remainingAmountOnClientBill);
              amountLeftToRedistribute -= remainingAmountOnClientBill;
            } else {
              redistributedAllocation = new Allocation(amountLeftToRedistribute);
              amountLeftToRedistribute = 0;
            }

            clientBill.addAllocation(redistributedAllocation);

            BillDAO.getInstance().persist(clientBill);
          }

          if (amountLeftToRedistribute == 0) {
            break;
          }
        }
      }
    } else {
      throw new BillNotFoundException();
    }
  }

}
