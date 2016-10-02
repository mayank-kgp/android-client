/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */

package com.mifos.mifosxdroid.online.grouploanaccount;

import android.R.layout;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mifos.mifosxdroid.R;
import com.mifos.mifosxdroid.core.MifosBaseActivity;
import com.mifos.mifosxdroid.core.ProgressableDialogFragment;
import com.mifos.mifosxdroid.core.util.Toaster;
import com.mifos.mifosxdroid.uihelpers.MFDatePicker;
import com.mifos.objects.accounts.loan.Loans;
import com.mifos.objects.organisation.LoanProducts;
import com.mifos.objects.templates.loans.GroupLoanTemplate;
import com.mifos.objects.templates.loans.TransactionProcessingStrategyOptions;
import com.mifos.services.data.GroupLoanPayload;
import com.mifos.utils.Constants;
import com.mifos.utils.DateHelper;
import com.mifos.utils.FragmentConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by nellyk on 1/22/2016.
 * <p/>
 * Use this  Fragment to Create and/or Update loan
 */
public class GroupLoanAccountFragment extends ProgressableDialogFragment
        implements MFDatePicker.OnDatePickListener, GroupLoanAccountMvpView {

    public final String LOG_TAG = getClass().getSimpleName();

    @BindView(R.id.sp_lproduct)
    Spinner sp_lproduct;

    @BindView(R.id.sp_loan_purpose)
    Spinner sp_loan_purpose;

    @BindView(R.id.tv_submittedon_date)
    TextView tv_submittedon_date;

    @BindView(R.id.et_client_external_id)
    EditText et_client_external_id;

    @BindView(R.id.et_nominal_annual)
    EditText et_nominal_annual;

    @BindView(R.id.et_principal)
    EditText et_principal;

    @BindView(R.id.et_loanterm)
    EditText et_loanterm;

    @BindView(R.id.et_numberofrepayments)
    EditText et_numberofrepayments;

    @BindView(R.id.et_repaidevery)
    EditText et_repaidevery;

    @BindView(R.id.sp_payment_periods)
    Spinner sp_payment_periods;

    @BindView(R.id.et_nominal_interest_rate)
    EditText et_nominal_interest_rate;

    @BindView(R.id.sp_amortization)
    Spinner sp_amortization;

    @BindView(R.id.sp_interestcalculationperiod)
    Spinner sp_interestcalculationperiod;

    @BindView(R.id.sp_fund)
    Spinner sp_fund;

    @BindView(R.id.sp_loan_officer)
    Spinner sp_loan_officer;

    @BindView(R.id.sp_interest_type)
    Spinner sp_interest_type;

    @BindView(R.id.sp_repaymentstrategy)
    Spinner sp_repaymentstrategy;

    @BindView(R.id.ck_calculateinterest)
    CheckBox ck_calculateinterest;

    @BindView(R.id.disbursementon_date)
    TextView tv_disbursementon_date;

    @BindView(R.id.bt_loan_submit)
    Button bt_loan_submit;

    @Inject
    GroupLoanAccountPresenter mGroupLoanAccountPresenter;

    GroupLoanTemplate mResponse;

    String submittion_date;
    String disbursementon_date;

    View rootView;

    private OnDialogFragmentInteractionListener mListener;
    private DialogFragment mfDatePicker;

    private int productId;
    private int groupId;
    private int loanPurposeId;
    private int loanTermFrequency;
    private int transactionProcessingStrategyId;
    private int amortizationTypeId;
    private int interestCalculationPeriodTypeId;
    private int fundId;
    private int loanOfficerId;
    private int interestTypeMethodId;

    private HashMap<String, Integer> loansNameIdHashMap = new HashMap<>();
    private HashMap<String, Integer> termFrequencyTypeIdHashMap = new HashMap<>();
    private HashMap<String, Integer> loanPurposeNameIdHashMap = new HashMap<>();
    private HashMap<String, Integer> interestCalculationPeriodTypeIdHashMap = new HashMap<>();
    private HashMap<String, Integer> amortizationTypeIdHashMap = new HashMap<>();
    private HashMap<String, Integer> transactionProcessingStrategyTypeIdHashMap = new HashMap<>();
    private HashMap<String, Integer> fundIdHashMap = new HashMap<>();
    private HashMap<String, Integer> loanOfficerIdHashMap = new HashMap<>();
    private HashMap<String, Integer> interestTypeMethodIdHashMap = new HashMap<>();


    public static GroupLoanAccountFragment newInstance(int groupId) {
        GroupLoanAccountFragment grouploanAccountFragment = new GroupLoanAccountFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.GROUP_ID, groupId);
        grouploanAccountFragment.setArguments(args);
        return grouploanAccountFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MifosBaseActivity) getActivity()).getActivityComponent().inject(this);
        if (getArguments() != null)
            groupId = getArguments().getInt(Constants.GROUP_ID);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        // Inflate the layout for this fragment
        if (getActivity().getActionBar() != null)
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        rootView = inflater.inflate(R.layout.fragment_add_loan, null);

        ButterKnife.bind(this, rootView);
        mGroupLoanAccountPresenter.attachView(this);

        inflateSubmissionDate();
        inflatedisbusmentDate();
        inflateLoansProductSpinner();


        disbursementon_date = tv_disbursementon_date.getText().toString();
        submittion_date = tv_submittedon_date.getText().toString();
        submittion_date = DateHelper.getDateAsStringUsedForCollectionSheetPayload
                (submittion_date).replace("-", " ");
        disbursementon_date = DateHelper.getDateAsStringUsedForCollectionSheetPayload
                (disbursementon_date).replace("-", " ");


        bt_loan_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GroupLoanPayload loansPayload = new GroupLoanPayload();
                loansPayload.setAllowPartialPeriodInterestCalcualtion(ck_calculateinterest
                        .isChecked());
                loansPayload.setAmortizationType(amortizationTypeId);
                loansPayload.setGroupId(groupId);
                loansPayload.setDateFormat("dd MMMM yyyy");
                loansPayload.setExpectedDisbursementDate(disbursementon_date);
                loansPayload.setInterestCalculationPeriodType(interestCalculationPeriodTypeId);
                loansPayload.setLoanType("group");
                loansPayload.setLocale("en");
                loansPayload.setNumberOfRepayments(et_numberofrepayments.getEditableText()
                        .toString());
                loansPayload.setPrincipal(et_principal.getEditableText().toString());
                loansPayload.setProductId(productId);
                loansPayload.setRepaymentEvery(et_repaidevery.getEditableText().toString());
                loansPayload.setSubmittedOnDate(submittion_date);
                loansPayload.setLoanPurposeId(loanPurposeId);
                loansPayload.setLoanTermFrequency(loanTermFrequency);
                loansPayload.setTransactionProcessingStrategyId(transactionProcessingStrategyId);

                initiateLoanCreation(loansPayload);

            }
        });

        return rootView;
    }

    @Override
    public void onDatePicked(String date) {
        tv_submittedon_date.setText(date);
        tv_disbursementon_date.setText(date);

    }

    private void inflateLoansProductSpinner() {
        mGroupLoanAccountPresenter.loadAllLoans();
    }


    private void inflateAmortizationSpinner(GroupLoanTemplate groupLoanTemplate) {

        List<com.mifos.objects.templates.loans.AmortizationTypeOptions>
                amortizationType = new ArrayList<>();
        // you can use this array to populate your spinner
        final ArrayList<String> amortizationTypeNames = new ArrayList<String>();
        amortizationType = groupLoanTemplate.getAmortizationTypeOptions();

        for (int i = 0; i < amortizationType.size(); i++) {
            com.mifos.objects.templates.loans.AmortizationTypeOptions
                    amortizationTypeObject = amortizationType.get(i);
            amortizationTypeNames.add(amortizationTypeObject.getValue());
            amortizationTypeIdHashMap.put(amortizationTypeObject.getValue(), amortizationTypeObject
                    .getId());
        }


        final ArrayAdapter<String> amortizationTypeAdapter =
                new ArrayAdapter<>(getActivity(),
                        layout.simple_spinner_item, amortizationTypeNames);
        amortizationTypeAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);
        sp_amortization.setAdapter(amortizationTypeAdapter);
        sp_amortization.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long
                    l) {
                amortizationTypeId = amortizationTypeIdHashMap.get(amortizationTypeNames
                        .get(i));
                Log.d("ammortization" + amortizationTypeNames.get(i), String.valueOf
                        (amortizationTypeId));
                if (amortizationTypeId != -1) {


                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_select_fund),
                            Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void inflateLoanPurposeSpinner() {
        mGroupLoanAccountPresenter.loadGroupLoansAccountTemplate(groupId, productId);
    }

    private void inflateInterestCalculationPeriodSpinner(GroupLoanTemplate groupLoanTemplate) {

        List<com.mifos.objects.templates.loans.InterestCalculationPeriodType>
                interestCalculationPeriodType = new ArrayList<>();
        // you can use this array to populate your spinner
        final ArrayList<String> interestCalculationPeriodTypeNames = new
                ArrayList<String>();
        //Try to get response body
        interestCalculationPeriodType = groupLoanTemplate.getInterestCalculationPeriodTypeOptions();
        for (int i = 0; i < interestCalculationPeriodType.size(); i++) {
            com.mifos.objects.templates.loans.InterestCalculationPeriodType
                    interestCalculationPeriodTypeObject = interestCalculationPeriodType.get(i);
            interestCalculationPeriodTypeNames
                            .add(interestCalculationPeriodTypeObject.getValue());
            interestCalculationPeriodTypeIdHashMap
                    .put(interestCalculationPeriodTypeObject.getValue()
                            , interestCalculationPeriodTypeObject.getId());
        }

        final ArrayAdapter<String> interestCalculationPeriodTypeAdapter =
                new ArrayAdapter<String>(getActivity(),
                        layout.simple_spinner_item,
                        interestCalculationPeriodTypeNames);

        interestCalculationPeriodTypeAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);
        sp_interestcalculationperiod.setAdapter(interestCalculationPeriodTypeAdapter);
        sp_interestcalculationperiod.setOnItemSelectedListener(new AdapterView
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long
                    l) {
                interestCalculationPeriodTypeId = interestCalculationPeriodTypeIdHashMap
                        .get(interestCalculationPeriodTypeNames.get(i));
                Log.d("interestCalculation " + interestCalculationPeriodTypeNames.get(i),
                        String.valueOf(interestCalculationPeriodTypeId));
                if (interestCalculationPeriodTypeId != -1) {


                } else {

                    Toast.makeText(getActivity(), getString(R.string
                            .error_select_interestCalculationPeriod), Toast.LENGTH_SHORT)
                            .show();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void inflatetransactionProcessingStrategySpinner(GroupLoanTemplate groupLoanTemplate) {

        List<TransactionProcessingStrategyOptions> transactionProcessingStrategyType = new
                ArrayList<>();
        // you can use this array to populate your spinner
        final ArrayList<String> transactionProcessingStrategyTypeNames = new
                ArrayList<String>();
        transactionProcessingStrategyType =
                groupLoanTemplate.getTransactionProcessingStrategyOptions();
        //Try to get response body

        List<TransactionProcessingStrategyOptions> transactionProcessingStrategyOptions = new
                ArrayList<>();
        transactionProcessingStrategyOptions =
                groupLoanTemplate.getTransactionProcessingStrategyOptions();

        for (int i = 0; i < transactionProcessingStrategyType.size(); i++) {
            TransactionProcessingStrategyOptions  transactionProcessingStrategyTypeObject =
                            transactionProcessingStrategyType.get(i);
            transactionProcessingStrategyTypeNames
                            .add(transactionProcessingStrategyTypeObject.getName());

            transactionProcessingStrategyTypeIdHashMap
                            .put(transactionProcessingStrategyTypeObject.getName(),
                                    transactionProcessingStrategyTypeObject.getId());
        }

        final ArrayAdapter<String> transactionProcessingStrategyAdapter =
                new ArrayAdapter<String>(getActivity(),
                        layout.simple_spinner_item,
                        transactionProcessingStrategyTypeNames);

        transactionProcessingStrategyAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);

        sp_repaymentstrategy.setAdapter(transactionProcessingStrategyAdapter);
        sp_repaymentstrategy.setOnItemSelectedListener(new AdapterView
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long
                    l) {
                transactionProcessingStrategyId =
                        transactionProcessingStrategyTypeIdHashMap.get
                                (transactionProcessingStrategyTypeNames.get(i));
                Log.d("transactionProcessing " + transactionProcessingStrategyTypeNames
                        .get(i), String.valueOf(transactionProcessingStrategyId));
                if (transactionProcessingStrategyId != -1) {


                } else {

                    Toast.makeText(getActivity(), getString(R.string
                            .error_select_transactionProcessingStrategy), Toast
                            .LENGTH_SHORT).show();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void inflateFrequencyPeriodSpinner(GroupLoanTemplate groupLoanTemplate) {

        List<com.mifos.objects.templates.loans.TermFrequencyTypeOptions> termFrequencyTypeOptions =
                new ArrayList<>();
        termFrequencyTypeOptions = groupLoanTemplate.getTermFrequencyTypeOptions();
        // you can use this array to populate your spinner
        final ArrayList<String>  termFrequencyTypeOptionsName = new ArrayList<String>();
        for (int i = 0; i < termFrequencyTypeOptions.size(); i++) {
            com.mifos.objects.templates.loans.TermFrequencyTypeOptions
                    termFrequencyOptionsTypesObject = termFrequencyTypeOptions.get(i);
            termFrequencyTypeOptionsName.add(termFrequencyOptionsTypesObject.getValue());
            termFrequencyTypeIdHashMap.put(termFrequencyOptionsTypesObject.getValue(),
                    termFrequencyOptionsTypesObject.getId
                    ());
        }

        final ArrayAdapter<String> termFrequencyTypeAdapter =
                new ArrayAdapter<>(getActivity(),
                        layout.simple_spinner_item, termFrequencyTypeOptionsName);
        termFrequencyTypeAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);
        sp_payment_periods.setAdapter(termFrequencyTypeAdapter);
        sp_payment_periods.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int i, long l) {

                        loanTermFrequency = termFrequencyTypeIdHashMap.get
                                (termFrequencyTypeOptionsName.get(i));
                        Log.d("termFrequencyTypeId" + termFrequencyTypeOptionsName.get(i),
                                String.valueOf(loanTermFrequency));
                        if (loanTermFrequency != -1) {


                        } else {

                            Toast.makeText(getActivity(), getString(R.string
                                            .error_select_fund),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
    }

    private void initiateLoanCreation(GroupLoanPayload loansPayload) {
        mGroupLoanAccountPresenter.createGroupLoanAccount(loansPayload);
    }

    public void inflateSubmissionDate() {
        mfDatePicker = MFDatePicker.newInsance(this);

        tv_submittedon_date.setText(MFDatePicker.getDatePickedAsString());

        tv_submittedon_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mfDatePicker.show(getActivity().getSupportFragmentManager(), FragmentConstants
                        .DFRAG_DATE_PICKER);
            }
        });

    }

    public void inflatedisbusmentDate() {
        mfDatePicker = MFDatePicker.newInsance(this);

        tv_disbursementon_date.setText(MFDatePicker.getDatePickedAsString());

        tv_disbursementon_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mfDatePicker.show(getActivity().getSupportFragmentManager(), FragmentConstants
                        .DFRAG_DATE_PICKER);
            }
        });

    }

    @Override
    public void showAllLoans(List<LoanProducts> loans) {
        /* Activity is null - Fragment has been detached; no need to do anything. */
        if (getActivity() == null) return;

        final List<String> loansList = new ArrayList<String>();
        for (LoanProducts loansname : loans) {
            loansList.add(loansname.getName());
            loansNameIdHashMap.put(loansname.getName(), loansname.getId());
        }
        ArrayAdapter<String> loansAdapter = new ArrayAdapter<>(getActivity(),
                layout.simple_spinner_item, loansList);
        loansAdapter.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        sp_lproduct.setAdapter(loansAdapter);
        sp_lproduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long
                    l) {
                productId = loansNameIdHashMap.get(loansList.get(i));
                Log.d("productId " + loansList.get(i), String.valueOf(productId));
                if (productId != -1) {

                    inflateLoanPurposeSpinner();

                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_select_loan),
                            Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void showLoanPurposeSpinner(GroupLoanTemplate groupLoanTemplate) {

        mResponse = groupLoanTemplate;

        inflateFrequencyPeriodSpinner(mResponse);
        inflateAmortizationSpinner(mResponse);
        inflateInterestCalculationPeriodSpinner(mResponse);
        inflatetransactionProcessingStrategySpinner(mResponse);
        inflateFundSpinner(mResponse);
        inflateLoanOfficerSpinner(mResponse);
        inflateInterestTypeSpinner(mResponse);

        List<com.mifos.objects.templates.loans.LoanPurposeOptions> loanPurposeOptionsType =
                new ArrayList<>();
        loanPurposeOptionsType = groupLoanTemplate.getLoanPurposeOptions();
        // you can use this array to populate your spinner
        final ArrayList<String> loanPurposeOptionsTypeNames = new ArrayList<String>();
        for (int i = 0; i < loanPurposeOptionsType.size(); i++) {
            com.mifos.objects.templates.loans.LoanPurposeOptions loanPurposeOptionsTypesObject =
                    loanPurposeOptionsType.get(i);
            loanPurposeOptionsTypeNames.add(loanPurposeOptionsTypesObject.getName());
            loanPurposeNameIdHashMap.put(loanPurposeOptionsTypesObject.getName(),
                    loanPurposeOptionsTypesObject.getId
                    ());
        }
        final ArrayAdapter<String> loanPTypeAdapter =
                new ArrayAdapter<>(getActivity(), layout.simple_spinner_item,
                        loanPurposeOptionsTypeNames);

        loanPTypeAdapter.setDropDownViewResource(layout.simple_spinner_dropdown_item);
        sp_loan_purpose.setAdapter(loanPTypeAdapter);
        sp_loan_purpose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long
                    l) {
                loanPurposeId = loanPurposeNameIdHashMap.get(loanPurposeOptionsTypeNames
                        .get(i));
                Log.d("loanpurpose" + loanPurposeOptionsTypeNames.get(i), String.valueOf
                        (loanPurposeId));
                if (loanPurposeId != -1) {


                } else {

                    Toast.makeText(getActivity(), getString(R.string.error_select_fund),
                            Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void inflateInterestTypeSpinner(GroupLoanTemplate groupLoanTemplate) {
        List<com.mifos.objects.templates.loans.InterestTypeOptions> interestTypeOptions =
                new ArrayList<>();
        interestTypeOptions = groupLoanTemplate.getInterestTypeOptions();
        // you can use this array to populate your spinner
        final ArrayList<String>  interestTypeOptionsName = new ArrayList<String>();
        for (int i = 0; i < interestTypeOptions.size(); i++) {
            com.mifos.objects.templates.loans.InterestTypeOptions interestTypeOptionsObject =
                    interestTypeOptions.get(i);
            interestTypeOptionsName.add(interestTypeOptionsObject.getValue());
            interestTypeMethodIdHashMap.put(interestTypeOptionsObject.getValue(),
                    interestTypeOptionsObject.getId
                    ());
        }

        final ArrayAdapter<String> interestTypeOptionsAdapter =
                new ArrayAdapter<String>(getActivity(),
                        layout.simple_spinner_item, interestTypeOptionsName);
        interestTypeOptionsAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);
        sp_interest_type.setAdapter(interestTypeOptionsAdapter);
        sp_interest_type.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int i, long l) {

                        loanOfficerId = interestTypeMethodIdHashMap.get
                                (interestTypeOptionsName.get(i));
                        Log.d("termFrequencyTypeId" + interestTypeOptionsName.get(i),
                                String.valueOf(loanTermFrequency));
                        if (loanTermFrequency != -1) {


                        } else {

                            Toast.makeText(getActivity(), getString(R.string
                                            .error_select_fund),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
    }

    private void inflateLoanOfficerSpinner(GroupLoanTemplate groupLoanTemplate) {
        List<com.mifos.objects.templates.loans.LoanOfficerOptions> loanOfficerOptions =
                new ArrayList<>();
        loanOfficerOptions = groupLoanTemplate.getLoanOfficerOptions();
        // you can use this array to populate your spinner
        final ArrayList<String>  loanOfficerName = new ArrayList<String>();
        for (int i = 0; i < loanOfficerOptions.size(); i++) {
            com.mifos.objects.templates.loans.LoanOfficerOptions loanOfficerOptionsTypesObject =
                    loanOfficerOptions.get(i);
            loanOfficerName.add(loanOfficerOptionsTypesObject.getDisplayName());
            loanOfficerIdHashMap.put(loanOfficerOptionsTypesObject.getDisplayName(),
                    loanOfficerOptionsTypesObject.getId
                    ());
        }

        final ArrayAdapter<String> loanOfficerOptionsAdapter =
                new ArrayAdapter<String>(getActivity(),
                        layout.simple_spinner_item, loanOfficerName);
        loanOfficerOptionsAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);
        sp_loan_officer.setAdapter(loanOfficerOptionsAdapter);
        sp_loan_officer.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int i, long l) {

                        loanOfficerId = loanOfficerIdHashMap.get
                                (loanOfficerName.get(i));
                        Log.d("termFrequencyTypeId" + loanOfficerName.get(i),
                                String.valueOf(loanTermFrequency));
                        if (loanTermFrequency != -1) {


                        } else {

                            Toast.makeText(getActivity(), getString(R.string
                                            .error_select_fund),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
    }

    private void inflateFundSpinner(GroupLoanTemplate groupLoanTemplate) {
        List<com.mifos.objects.templates.loans.FundOptions> fundOptions = new ArrayList<>();
        fundOptions = groupLoanTemplate.getFundOptions();
        // you can use this array to populate your spinner
        final ArrayList<String>  fundOptionsName = new ArrayList<String>();
        for (int i = 0; i < fundOptions.size(); i++) {
            com.mifos.objects.templates.loans.FundOptions termFrequencyOptionsTypesObject =
                    fundOptions.get(i);
            fundOptionsName.add(termFrequencyOptionsTypesObject.getName());
            fundIdHashMap.put(termFrequencyOptionsTypesObject.getName(),
                    termFrequencyOptionsTypesObject.getId
                    ());
        }

        final ArrayAdapter<String> fundOptionsAdapter =
                new ArrayAdapter<String>(getActivity(),
                        layout.simple_spinner_item, fundOptionsName);
        fundOptionsAdapter.setDropDownViewResource(
                layout.simple_spinner_dropdown_item);
        sp_fund.setAdapter(fundOptionsAdapter);
        sp_fund.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int i, long l) {

                        fundId = fundIdHashMap.get
                                (fundOptionsName.get(i));
                        Log.d("termFrequencyTypeId" + fundOptionsName.get(i),
                                String.valueOf(loanTermFrequency));
                        if (loanTermFrequency != -1) {


                        } else {

                            Toast.makeText(getActivity(), getString(R.string
                                            .error_select_fund),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
    }

    @Override
    public void showGroupLoansAccountCreatedSuccessfully(Loans loans) {
        Toast.makeText(getActivity(), "The Loan has been submitted for Approval", Toast
                .LENGTH_LONG).show();
    }

    @Override
    public void showFetchingError(String s) {
        Toaster.show(rootView, s);
    }

    @Override
    public void showProgressbar(boolean b) {
        showProgress(b);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGroupLoanAccountPresenter.detachView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnDialogFragmentInteractionListener {


    }
}
