package com.hepl.budgie.service.iiy;

import java.util.List;

import com.hepl.budgie.entity.iiy.IdeaCategory;

public interface IdeaCategoryService {
    void addIdeaCategory(IdeaCategory request);

    List<IdeaCategory> fetchIdeaCategoryList();

    void updateIdeaCategory(IdeaCategory request);

    void deleteIdeaCategory(String id);

    void updateStatusIdeaCategory(String id);

    List<IdeaCategory> fetchIdeaCategory();
}
