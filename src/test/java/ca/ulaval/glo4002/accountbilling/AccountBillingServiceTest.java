package ca.ulaval.glo4002.accountbilling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class AccountBillingServiceTest {

  private static final int BILL_AMOUNT = 25;
  private static final ClientId CLIENT_ID = new ClientId(3L);
  private static final BillId BILL_ID = new BillId(18L);

  private AccountBillingService accountBillingService;
  private List<Bill> persistedBills;
  private List<Bill> clientBills;

  private Bill bill;
  @Mock
  private BillDAO billDao;

  @Before
  public void setUp() {
    accountBillingService = new AccountBillingService();
    persistedBills = new ArrayList<>();
    bill = new Bill(CLIENT_ID, BILL_AMOUNT);
    clientBills = new ArrayList<>();

    BillDAO.setInstance(billDao);
    BDDMockito.willReturn(bill).given(billDao).findBill(BILL_ID);
    BDDMockito.willReturn(clientBills).given(billDao).findAllByClient(CLIENT_ID);
    BDDMockito.willAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        Bill billArg = (Bill) args[0];
        persistedBills.add(billArg);
        return null;
      }
    }).given(billDao).persist(bill);
  }

  @Test(expected = BillNotFoundException.class)
  public void givenUnexistingBillIdWhenCancelInvoiceThenThrowsException() {
    BDDMockito.willReturn(null).given(billDao).findBill(BILL_ID);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);
  }

  @Test
  public void givenBillNotCanceledWhenCancelInvoiceThenBillIsCanceled() {
    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertTrue(bill.isCancelled());
  }

  @Test
  public void givenBillCanceledWhenCancelInvoiceThenNothingIsDone() {
    bill.cancel();

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);
  }

  @Test
  public void whenCancelInvoiceThenPersistBill() {
    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertTrue(persistedBills.contains(bill));
  }

  @Test
  public void givenBillHasOneAllocationAndClientHasOneOtherUnpaidBillWhenCancelInvoiceThenAllocationIsDistributedToOtherBill() {
    bill.addAllocation(new Allocation(BILL_AMOUNT));
    Bill clientBill = new Bill(CLIENT_ID, 30);
    clientBills.add(clientBill);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertEquals(30 - BILL_AMOUNT, clientBill.getRemainingAmount());
  }

  @Test
  public void givenBillHasCancelledBillInClientBillsWhenCancelInvoiceThenCurrentBillIsNotConsidered() {
    bill.addAllocation(new Allocation(10));
    Bill clientBill = new Bill(CLIENT_ID, BILL_AMOUNT);
    clientBills.add(clientBill);
    clientBills.add(bill);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertEquals(BILL_AMOUNT - 10, bill.getRemainingAmount());
  }

  @Test
  public void givenBillHasOneAllocationAndClientHasOtherSmallUnpaidBillsWhenCancellingThenAllocationIsDistributedToTheTwoOtherBills() {
    bill = new Bill(CLIENT_ID, 10);
    bill.addAllocation(new Allocation(10));
    BDDMockito.willReturn(bill).given(billDao).findBill(BILL_ID);
    Bill smallUnpaidBill = new Bill(CLIENT_ID, 2);
    Bill unpaidBill = new Bill(CLIENT_ID, 20);
    clientBills.add(smallUnpaidBill);
    clientBills.add(unpaidBill);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertEquals(0, smallUnpaidBill.getRemainingAmount());
    assertEquals(12, unpaidBill.getRemainingAmount());
  }

  @Test
  public void givenBillHasMultipleAllocations_whenCancelling_thenAllAllocationsAreRedistributed() {
    bill = new Bill(CLIENT_ID, 10);
    bill.addAllocation(new Allocation(6));
    bill.addAllocation(new Allocation(4));
    BDDMockito.willReturn(bill).given(billDao).findBill(BILL_ID);
    Bill unpaidBill = new Bill(CLIENT_ID, 20);
    clientBills.add(unpaidBill);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertEquals(10, unpaidBill.getRemainingAmount());
  }

  @Test
  public void givenBillHasAllocationsToRedistribute_whenCancelling_thenBillArePersisted() {
    bill = new Bill(CLIENT_ID, 10);
    bill.addAllocation(new Allocation(10));
    BDDMockito.willReturn(bill).given(billDao).findBill(BILL_ID);
    Bill unpaidBill = new Bill(CLIENT_ID, 20);
    clientBills.add(unpaidBill);
    BDDMockito.willAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        Bill billArg = (Bill) args[0];
        persistedBills.add(billArg);
        return null;
      }
    }).given(billDao).persist(unpaidBill);

    accountBillingService.cancelInvoiceAndRedistributeFunds(BILL_ID);

    assertTrue(persistedBills.contains(unpaidBill));
  }

}
