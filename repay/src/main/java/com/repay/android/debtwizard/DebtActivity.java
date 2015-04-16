package com.repay.android.debtwizard;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.repay.lib.manager.DatabaseManager;
import com.repay.model.Debt;
import com.repay.lib.builder.DebtBuilder;
import com.repay.model.Person;

/**
 * Property of Matt Allen
 * mattallen092@gmail.com
 * http://mattallensoftware.co.uk/
 * <p/>
 * This software is distributed under the Apache v2.0 license and use
 * of the Repay name may not be used without explicit permission from the project owner.
 */

public abstract class DebtActivity extends ActionBarActivity
{
	public static final String FRIEND = "friend";
	public static final String DEBT = "debt";
	public static final String DEBT_REPAID_TEXT = "Repaid";
	private static final String DEBT_BUILDER = "builder";

	protected DatabaseManager mDB;

	protected Person mPerson;
	protected Debt mDebt;

	protected DebtBuilder mBuilder;

	protected boolean isEditing = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Do some instantiation here

		if (savedInstanceState != null && savedInstanceState.get(DEBT_BUILDER) != null)
		{
			mBuilder = (DebtBuilder) savedInstanceState.get(DEBT_BUILDER);
		} else
		{
			mBuilder = new DebtBuilder();

			if (getIntent().getExtras() != null && getIntent().getExtras().get(FRIEND) != null)
			{
				mPerson = (Person) getIntent().getExtras().get(FRIEND);
				mBuilder.addSelectedFriend(mPerson);
			}

			if (getIntent().getExtras() != null && getIntent().getExtras().get(DEBT) != null)
			{
				mDebt = (Debt) getIntent().getExtras().get(DEBT);
				mBuilder.setAmount(mDebt.getAmount());
				mBuilder.setDescription(mDebt.getDescription());
				mBuilder.setDate(mDebt.getDate());
			}
		}

		mDB = new DatabaseManager(this);
	}

	public DebtBuilder getDebtBuilder()
	{
		return mBuilder;
	}

	public DatabaseManager getDBHandler()
	{
		return mDB;
	}

	public void save()
	{
		if (isEditing)
		{
			// Subtract the old amount
			mPerson.setDebt(mPerson.getDebt().subtract(mDebt.getAmount()));
			// Get the newly entered data
			mDebt.setAmount(mBuilder.getAmountToApply());
			mDebt.setDescription(mBuilder.getDescription());
			mDB.updateDebt(mDebt);
			// Add the new amount
			mPerson.setDebt(mPerson.getDebt().add(mDebt.getAmount()));
			mDB.updateFriendRecord(mPerson);
			finish();
		} else
		{
			// Add the debts into the DB
			for (Debt debt : mBuilder.getNewDebts())
			{
				mDB.addDebt(debt.getRepayID(), debt.getAmount(), debt.getDescription());
			}
			// Then update the friend objects
			for (Person person : mBuilder.getUpdatedFriends())
			{
				mDB.updateFriendRecord(person);
			}
		}

		finish(); // Return to friend overview
	}

	public abstract void onNextButtonClick(View v);

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putSerializable(DEBT_BUILDER, mBuilder);
	}
}
