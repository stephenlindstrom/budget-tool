import PropTypes from "prop-types";

export default function CategorySelect({
  id,
  name = "categoryId",
  value,
  categories = [],
  loading = false, 
  onChange,
  onAddCategoryClick,
  selectRef,
  placeholder = "Select a category",
  className,
  ...ariaProps
}) {
  const normalizedValue = value === "" ? "" : String(value);
  const list = Array.isArray(categories) ? categories : [];

  return (
    <select
      id={id}
      name={name}
      ref={selectRef}
      value={normalizedValue}
      onChange={(e) => {
        if (e.target.value === "__add__") {
          // Trigger the modal (or any UI) to create a category
          onAddCategoryClick?.();
          return;
        }
        onChange?.(e);
      }}
      required
      disabled={loading}
      aria-busy={loading || undefined}
      className={className}
      {...ariaProps}
    >
      <option value="">{loading ? "Loading..." : placeholder}</option>
      <option value="__add__">Add new category...</option>
      {list.map((c) => (
        <option key={c.id} value={String(c.id)}>
          {c.name}
        </option>
      ))}
    </select>
  );
}

CategorySelect.propTypes = {
  id: PropTypes.string.isRequired,
  name: PropTypes.string,
  value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  categories: PropTypes.arrayOf(
    PropTypes.shape({ id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired, name: PropTypes.string.isRequired, type: PropTypes.string.isRequired })
  ),
  loading: PropTypes.bool,
  onChange: PropTypes.func,
  onAddCategoryClick: PropTypes.func,
  selectRef: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.shape({ current: PropTypes.instanceOf(Element) }),
  ]),
  placeholder: PropTypes.string,
  className: PropTypes.string,
};