/*
 * Copyright (c) 2014, KJFrameForAndroid 张涛 (kymjs123@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kymjs.aframe.database;

import java.util.ArrayList;
import java.util.List;

/**
 * 一对多延迟加载类<br>
 * <b>创建时间</b> 2014-8-15
 * 
 * @param <O>
 *            宿主实体的class
 * @param <M>
 *            多放实体class
 * @version 1.0
 */
public class OneToManyLazyLoader<O, M> {
    O ownerEntity;
    Class<O> ownerClazz;
    Class<M> listItemClazz;
    KJDB db;

    public OneToManyLazyLoader(O ownerEntity, Class<O> ownerClazz,
            Class<M> listItemclazz, KJDB db) {
        this.ownerEntity = ownerEntity;
        this.ownerClazz = ownerClazz;
        this.listItemClazz = listItemclazz;
        this.db = db;
    }

    List<M> entities;

    /**
     * 如果数据未加载，则调用loadOneToMany填充数据
     * 
     * @return
     */
    public List<M> getList() {
        if (entities == null) {
            this.db.loadOneToMany((O) this.ownerEntity, this.ownerClazz,
                    this.listItemClazz);
        }
        if (entities == null) {
            entities = new ArrayList<M>();
        }
        return entities;
    }

    public void setList(List<M> value) {
        entities = value;
    }

}
