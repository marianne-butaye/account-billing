package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

  public void cancelInvoiceAndRedistributeFunds(BillId billId) {
    Bill billToCancel = BillDAO.getInstance().findBill(billId);
    if (billToCancel != null) {
      ClientId clientId = billToCancel.getClientId();

      if (!billToCancel.isCancelled()) {
        billToCancel.cancel();
      }
      BillDAO.getInstance().persist(billToCancel);

      List<Allocation> allocationsToRedistribute = billToCancel.getAllocations();

      for (Allocation allocationToRedistribute : allocationsToRedistribute) {
        List<Bill> unpaidBills = BillDAO.getInstance().findAllByClient(clientId);
        int amountLeftToRedistribute = allocationToRedistribute.getAmount();

        for (Bill unpaidBill : unpaidBills) {
          if (billToCancel != unpaidBill) {
            int amountLeftToPay = unpaidBill.getRemainingAmount();
            Allocation amountRedistributed;
            if (amountLeftToPay <= amountLeftToRedistribute) {
              amountRedistributed = new Allocation(amountLeftToPay);
              amountLeftToRedistribute -= amountLeftToPay;
            } else {
              amountRedistributed = new Allocation(amountLeftToRedistribute);
              amountLeftToRedistribute = 0;
            }

            unpaidBill.addAllocation(amountRedistributed);

            BillDAO.getInstance().persist(unpaidBill);
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
