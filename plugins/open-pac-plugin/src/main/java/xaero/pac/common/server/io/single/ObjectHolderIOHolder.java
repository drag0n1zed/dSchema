/*
 * Open Parties and Claims - adds chunk claims and player parties to Minecraft
 * Copyright (C) 2023, Xaero <xaero1996@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * (LGPL-3.0-only) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU Lesser General Public License
 * and the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package xaero.pac.common.server.io.single;

import com.google.common.collect.Lists;
import xaero.pac.common.server.io.ObjectManagerIOManager;
import xaero.pac.common.server.io.ObjectManagerIOObject;

import java.util.ArrayList;

public abstract class ObjectHolderIOHolder
<
	T extends ObjectManagerIOObject,
	M extends ObjectHolderIOHolder<T, M>
> implements ObjectManagerIOManager<T, M> {

	public abstract T getObject();

	public abstract void setObject(T object);

	@Override
	public void addToSave(T object) {
	}

	@Override
	public Iterable<T> getToSave() {
		T object = getObject();
		if(object != null && object.isDirty())
			return Lists.newArrayList(object);
		return new ArrayList<>();
	}
}
