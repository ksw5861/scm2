const showAllMenus = import.meta.env.VITE_SHOW_ALL_MENUS === 'true';

export const filterMenuByRole = (menu, userRoles) => {
  if (showAllMenus) return menu;
  
  return menu
    .map(section => {
      if (!section.items) return section;

      const filteredItems = section.items.filter(item => {
        if (!item.roles) return true;
        return item.roles.some(role => userRoles.includes(role));
      });

      if (filteredItems.length > 0) {
        return {
          ...section,
          items: filteredItems
        };
      }

      return null;
    })
    .filter(Boolean);
}
