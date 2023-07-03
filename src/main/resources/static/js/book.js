$(document).ready(function () {
    $('.update-quantity-btn').prop('disabled', true);

    $(document).on('change', 'input[type="number"].label-style', function () {
        let inputElement = $(this);
        let updateButton = inputElement.siblings('.update-quantity-btn');
        let currentQuantity = inputElement.data('current-quantity');
        let newQuantity = inputElement.val();

        if (currentQuantity !== newQuantity) {
            updateButton.prop('disabled', false);
        } else {
            updateButton.prop('disabled', true);
        }
    });
    // 도서 재고 수량 변경
    $(document).on('click', '.update-quantity-btn', function () {
        let bookId = $(this).siblings("input[type='hidden']").val();
        let newQuantity = $(this).closest("tr").find("input[type='number']").val();

        if (newQuantity < 0 || !/^\d+$/.test(newQuantity)) {
            alert("수량은 0 이상의 숫자이어야 합니다.");
            return;
        }
        $.ajax({
            url: '/admin/books/' + bookId + '?quantity=' + newQuantity,
            type: 'PUT',
            data: {quantity: newQuantity},
            success: function (response) {
                console.log(response);
                alert("수량이 " + newQuantity + "권으로 변경되었습니다.")
                location.reload();
            },
            error: function (xhr) {
                let errorMessage = xhr.responseText;
                alert(errorMessage);
            }
        });
    });

    $(document).on('click', '.delete-book-btn', function () {
        let bookId = $(this).siblings("input[type='hidden']").val();
        if (confirm('정말로 도서를 삭제하시겠습니까?')) {
            $.ajax({
                url: '/admin/books/' + bookId,
                type: 'DELETE',
                success: function (response) {
                    console.log(response);
                    location.reload();
                },
                error: function (xhr) {
                    let errorMessage = xhr.responseText;
                    alert(errorMessage);
                }
            });
        }
    });

})
;