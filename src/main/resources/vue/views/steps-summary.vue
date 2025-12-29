<template id="steps-summary">
  <app-layout>

    <div class="card bg-light mb-3">
      <div class="card-header">
        <div class="row">
          <div class="col-6">Steps</div>
          <div class="col" align="right">
            <button class="btn btn-info btn-simple btn-link" @click="hideForm = !hideForm">
              <i class="fa fa-plus"></i>
            </button>
          </div>
        </div>
      </div>

      <div class="card-body" :class="{ 'd-none': hideForm }">
        <form>
          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">Steps</span>
            </div>
            <input type="number" class="form-control" v-model="formData.steps" />
          </div>

          <div class="input-group mb-3">
            <div class="input-group-prepend">
              <span class="input-group-text">Date</span>
            </div>
            <input type="text" class="form-control" v-model="formData.date" />
          </div>
        </form>

        <button class="btn btn-info btn-simple btn-link" @click="addSteps">Add Steps</button>
      </div>
    </div>

    <div class="list-group list-group-flush">
      <div class="list-group-item d-flex align-items-start"
           v-for="(s,index) in entries" :key="index">

        <div class="mr-auto p-2">
          {{ s.date }} — {{ s.steps }} steps
        </div>

        <div class="p2">
          <button class="btn btn-info btn-simple btn-link" @click="editSteps(s)">
            <i class="fa fa-pencil"></i>
          </button>

          <button class="btn btn-info btn-simple btn-link" @click="deleteSteps(s.id, index)">
            <i class="fas fa-trash"></i>
          </button>
        </div>

      </div>
    </div>

  </app-layout>
</template>

<script>
app.component("steps-summary", {
  template: "#steps-summary",
  data: () => ({
    entries: [],
    formData: [],
    hideForm: true,
    editingId: null
  }),

  created() {
    const id = this.$javalin.pathParams["user-id"];
    axios.get(`/api/users/${id}/steps`)
        .then(res => this.entries = res.data);
  },

  methods: {
    addSteps() {
      const id = this.$javalin.pathParams["user-id"];
      axios.post(`/api/users/${id}/steps`, {
        steps: Number(this.formData.steps),
        date: this.formData.date,
        userId: id
      })
          .then(res => {
            this.entries.push(res.data);
            this.hideForm = true;
          });
    },

    editSteps(s) {
      this.editingId = s.id;
      this.formData.steps = s.steps;
      this.formData.date = s.date;
      this.hideForm = false;
    },

    deleteSteps(id, index) {
      axios.delete(`/api/steps/${id}`)
          .then(() => this.entries.splice(index, 1));
    }
  }
});
</script>
