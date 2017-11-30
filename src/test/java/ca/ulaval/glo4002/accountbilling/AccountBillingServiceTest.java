package ca.ulaval.glo4002.accountbilling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountBillingServiceTest {

  private static final ClientId CLIENT_ID = new ClientId(3L);

  private static final BillId BILL_ID = new BillId(18L);

  private AccountBillingService accountBillingService;
  private Bill clientBill;

  @Mock
  private Bill bill;
  @Mock
  private BillDAO billDao;

  @Before
  public void setUp() {
    accountBillingService = new AccountBillingService();
    BDDMockito.willReturn(true).given(bill).isCancelled();
    BDDMockito.willReturn(CLIENT_ID).given(bill).getClientId();

    List<Bill> clientBills = new ArrayList<>();
    clientBill = new Bill(CLIENT_ID, 1);
    clientBills.add(clientBill);

    BillDAO.setInstance(billDao);
    BDDMockito.willReturn(bill).given(billDao).findBill(BILL_ID);
    BDDMockito.willReturn(clientBills).given(billDao).findAllByClient(CLIENT_ID);

    List<Allocation> allocations = new ArrayList<>();
    allocations.add(new Allocation(3));
    BDDMockito.willReturn(allocations).given(bill).getAllocations();
  }

  @Test(expected = BillNotFoundException.class)
  public void givenUnexistingBillIdWhenCancelInvoiceThenThrowsException() {
    BDDMockito.willReturn(null).given(billDao).findBill(BILL_ID);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);
  }

  @Test
  public void givenBillNotCanceledWhenCancelInvoiceThenVerifyBillIsCanceled() {
    BDDMockito.willReturn(false).given(bill).isCancelled();

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    BDDMockito.verify(bill).cancel();
  }

  @Test
  public void givenBillCanceledWhenCancelInvoiceThenVerifyBillIsNotCanceledAgain() {
    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    BDDMockito.verify(bill, never()).cancel();
  }

  @Test
  public void givenDifferentBillFromCurrentBillAndOneClientBillWhenCancelInvoiceThenVerifyAllocationIsAdded() {
    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertEquals(1, clientBill.getAllocations().size());
  }

  @Test
  @Ignore
  public void givenDifferentBillFromCurrentBillAndRemainingAmountBiggerThanAmountLeftToRedistributeWhenCancelInvoiceThenVerifyAllocationAddedIs() {
    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    BDDMockito.verify(bill, never()).cancel();
  }

}
